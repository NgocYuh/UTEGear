package com.utegear.service;

import com.utegear.dto.order.CheckoutItemRequest;
import com.utegear.dto.order.CheckoutQuoteResponse;
import com.utegear.dto.order.CheckoutRequest;
import com.utegear.dto.order.CouponQuote;
import com.utegear.dto.order.OrderResponse;
import com.utegear.dto.order.PlaceOrderRequest;
import com.utegear.entity.Address;
import com.utegear.entity.Cart;
import com.utegear.entity.Coupon;
import com.utegear.entity.Order;
import com.utegear.entity.ProductImage;
import com.utegear.entity.ProductStatus;
import com.utegear.entity.ProductVariant;
import com.utegear.entity.ShippingMethod;
import com.utegear.entity.Store;
import com.utegear.entity.StoreInventory;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.AddressRepository;
import com.utegear.repository.CartRepository;
import com.utegear.repository.CouponRepository;
import com.utegear.repository.OrderRepository;
import com.utegear.repository.ProductVariantRepository;
import com.utegear.repository.StoreInventoryRepository;
import com.utegear.repository.StoreRepository;
import com.utegear.util.TransactionCallbacks;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutService {

    private static final BigDecimal HOME_DELIVERY_FEE = new BigDecimal("30000.00");

    private final AddressRepository addresses;
    private final StoreRepository stores;
    private final ProductVariantRepository variants;
    private final StoreInventoryRepository inventories;
    private final CouponRepository coupons;
    private final CouponService couponService;
    private final CartRepository carts;
    private final OrderRepository orders;
    private final CheckoutQuoteStore quoteStore;
    private final TransactionCallbacks transactionCallbacks;

    public CheckoutService(AddressRepository addresses, StoreRepository stores,
            ProductVariantRepository variants, StoreInventoryRepository inventories,
            CouponRepository coupons, CouponService couponService, CartRepository carts,
            OrderRepository orders, CheckoutQuoteStore quoteStore, TransactionCallbacks transactionCallbacks) {
        this.addresses = addresses;
        this.stores = stores;
        this.variants = variants;
        this.inventories = inventories;
        this.coupons = coupons;
        this.couponService = couponService;
        this.carts = carts;
        this.orders = orders;
        this.quoteStore = quoteStore;
        this.transactionCallbacks = transactionCallbacks;
    }

    @Transactional(readOnly = true)
    public CheckoutQuoteResponse quote(User user, CheckoutRequest request) {
        requireUser(user);
        addresses.findByIdAndUserId(request.addressId(), user.getId()).orElseThrow(() -> notFound("ADDRESS_NOT_FOUND", "Không tìm thấy địa chỉ giao hàng"));
        Store store = requireStore(request.fulfillmentStoreId());
        List<Line> lines = loadLines(store.getId(), request.items(), false);
        BigDecimal subtotal = subtotal(lines);
        CouponQuote coupon = couponQuote(request.couponCode(), subtotal, false);
        BigDecimal discount = coupon == null ? BigDecimal.ZERO : coupon.discountAmount();
        CheckoutQuoteStore.Quote saved = quoteStore.create(user.getId(), request);
        return new CheckoutQuoteResponse(
                saved.token(), saved.expiresAt(), lines.stream().map(Line::response).toList(), subtotal,
                discount, HOME_DELIVERY_FEE, subtotal.subtract(discount).add(HOME_DELIVERY_FEE), coupon,
                new CheckoutQuoteResponse.StoreRef(store.getId(), store.getName()), List.of());
    }

    @Transactional
    public OrderResponse place(User user, PlaceOrderRequest request) {
        requireUser(user);
        quoteStore.require(user.getId(), request);
        Address address = addresses.findByIdAndUserId(request.addressId(), user.getId())
                .orElseThrow(() -> notFound("ADDRESS_NOT_FOUND", "Không tìm thấy địa chỉ giao hàng"));
        Store store = requireStore(request.fulfillmentStoreId());
        Cart cart = carts.findWithItemsByUserId(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "CART_EMPTY", "Giỏ hàng đang trống"));
        requireCartMatches(cart, request.items());
        List<Line> lines = loadLines(store.getId(), request.items(), true);
        BigDecimal subtotal = subtotal(lines);
        CouponQuote couponQuote = couponQuote(request.couponCode(), subtotal, true);
        Coupon coupon = couponQuote == null ? null : coupons.findByCodeIgnoreCase(couponQuote.code()).orElseThrow();
        BigDecimal discount = couponQuote == null ? BigDecimal.ZERO : couponQuote.discountAmount();

        Order order = new Order(orderCode(), user, store, coupon, address, ShippingMethod.HOME_DELIVERY, request.note());
        lines.forEach(line -> order.addItem(line.variant(), line.quantity(), primaryImage(line.variant())));
        order.applyPricing(discount, HOME_DELIVERY_FEE);
        order.createPayment(request.paymentMethod());
        Order saved = orders.save(order);

        cart.clear();
        carts.save(cart);
        transactionCallbacks.afterCommit(() -> quoteStore.remove(request.quoteToken()));
        return OrderResponse.from(saved);
    }

    private List<Line> loadLines(Long storeId, List<CheckoutItemRequest> requested, boolean lock) {
        Set<Long> ids = new HashSet<>();
        List<Line> lines = new ArrayList<>();
        for (CheckoutItemRequest item : requested) {
            if (!ids.add(item.variantId())) {
                throw validation("Mỗi biến thể chỉ được xuất hiện một lần");
            }
            ProductVariant variant = variants.findById(item.variantId())
                    .orElseThrow(() -> notFound("VARIANT_NOT_FOUND", "Không tìm thấy biến thể sản phẩm"));
            if (!variant.isActive() || variant.getProduct().getStatus() != ProductStatus.ACTIVE) {
                throw new ApiException(HttpStatus.CONFLICT, "VARIANT_UNAVAILABLE", "Biến thể không còn khả dụng");
            }
            StoreInventory inventory = (lock
                    ? inventories.findLocked(storeId, item.variantId())
                    : inventories.findByStoreIdAndVariantId(storeId, item.variantId()))
                    .orElseThrow(() -> insufficient(item.variantId()));
            if (inventory.getQuantity() < item.quantity()) {
                throw insufficient(item.variantId());
            }
            if (lock) {
                inventory.decrease(item.quantity());
            }
            BigDecimal unitPrice = variant.getProduct().getEffectivePrice().add(variant.getPriceAdjustment());
            lines.add(new Line(variant, item.quantity(), unitPrice));
        }
        return lines;
    }

    private CouponQuote couponQuote(String code, BigDecimal subtotal, boolean use) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return use ? couponService.use(code, subtotal) : couponService.validate(code, subtotal);
    }

    private Store requireStore(Long id) {
        return stores.findByIdAndActiveTrue(id)
                .orElseThrow(() -> notFound("STORE_NOT_FOUND", "Không tìm thấy cửa hàng đang hoạt động"));
    }

    private BigDecimal subtotal(List<Line> lines) {
        return lines.stream().map(Line::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void requireCartMatches(Cart cart, List<CheckoutItemRequest> requested) {
        if (cart.getItems().size() != requested.size()) {
            throw new ApiException(HttpStatus.CONFLICT, "CART_CHANGED", "Giỏ hàng đã thay đổi, vui lòng báo giá lại");
        }
        boolean matches = requested.stream().allMatch(item -> cart.getItems().stream().anyMatch(cartItem ->
                cartItem.getVariant().getId().equals(item.variantId()) && cartItem.getQuantity() == item.quantity()));
        if (!matches) {
            throw new ApiException(HttpStatus.CONFLICT, "CART_CHANGED", "Giỏ hàng đã thay đổi, vui lòng báo giá lại");
        }
    }

    private String primaryImage(ProductVariant variant) {
        return variant.getProduct().getImages().stream().filter(ProductImage::isPrimary).findFirst()
                .or(() -> variant.getProduct().getImages().stream().findFirst())
                .map(ProductImage::getSecureUrl).orElse(null);
    }

    private String orderCode() {
        return "UTE-" + OffsetDateTime.now(ZoneOffset.UTC).toEpochSecond() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void requireUser(User user) {
        if (user == null || user.getId() == null) {
            throw validation("Người dùng hợp lệ là bắt buộc");
        }
    }

    private ApiException insufficient(Long variantId) {
        return new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", "Không đủ tồn kho cho biến thể " + variantId);
    }

    private ApiException validation(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
    }

    private ApiException notFound(String code, String message) {
        return new ApiException(HttpStatus.NOT_FOUND, code, message);
    }

    private record Line(ProductVariant variant, int quantity, BigDecimal unitPrice) {
        BigDecimal subtotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
        CheckoutQuoteResponse.Item response() {
            String image = variant.getProduct().getImages().stream().filter(ProductImage::isPrimary).findFirst()
                    .or(() -> variant.getProduct().getImages().stream().findFirst())
                    .map(ProductImage::getSecureUrl).orElse(null);
            return new CheckoutQuoteResponse.Item(variant.getId(), variant.getSku(), variant.getProduct().getName(),
                    unitPrice, quantity, subtotal(), image);
        }
    }
}
