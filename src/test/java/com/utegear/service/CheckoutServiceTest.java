package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.dto.order.CheckoutItemRequest;
import com.utegear.dto.order.CheckoutQuoteResponse;
import com.utegear.dto.order.CheckoutRequest;
import com.utegear.dto.order.PlaceOrderRequest;
import com.utegear.entity.Address;
import com.utegear.entity.Cart;
import com.utegear.entity.Category;
import com.utegear.entity.PaymentMethod;
import com.utegear.entity.Product;
import com.utegear.entity.ProductVariant;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CheckoutServiceTest {

    private AddressRepository addresses;
    private StoreRepository stores;
    private ProductVariantRepository variants;
    private StoreInventoryRepository inventories;
    private CouponService couponService;
    private CartRepository carts;
    private OrderRepository orders;
    private CheckoutQuoteStore quoteStore;
    private CheckoutService service;
    private User user;
    private Address address;
    private Store store;
    private ProductVariant variant;
    private StoreInventory inventory;
    private CheckoutRequest quoteRequest;

    @BeforeEach
    void setUp() {
        addresses = mock(AddressRepository.class);
        stores = mock(StoreRepository.class);
        variants = mock(ProductVariantRepository.class);
        inventories = mock(StoreInventoryRepository.class);
        CouponRepository coupons = mock(CouponRepository.class);
        couponService = new CouponService(coupons);
        carts = mock(CartRepository.class);
        orders = mock(OrderRepository.class);
        quoteStore = new CheckoutQuoteStore();
        service = new CheckoutService(addresses, stores, variants, inventories, coupons,
                couponService, carts, orders, quoteStore, new TransactionCallbacks());

        user = new User("Nguyễn Văn An", "an@example.test", null, "hash");
        ReflectionTestUtils.setField(user, "id", 7L);
        address = new Address(user, "Nguyễn Văn An", "0900000000", "01 Võ Văn Ngân",
                "Linh Chiểu", "Thủ Đức", "TP.HCM", "Nhà");
        ReflectionTestUtils.setField(address, "id", 3L);
        store = new Store("UTEGear Thủ Đức", "01 Võ Văn Ngân", "TP.HCM", "Thủ Đức",
                "0900000001", null, null, LocalTime.of(8, 0), LocalTime.of(21, 0), null,
                null, null, true);
        ReflectionTestUtils.setField(store, "id", 2L);
        Product product = new Product("Bàn phím K87", "ban-phim-k87",
                new Category("Bàn phím", "ban-phim", null, 0), null,
                new BigDecimal("1000000"), null);
        variant = new ProductVariant("K87-DEFAULT", Map.of(), true, BigDecimal.ZERO);
        product.addVariant(variant);
        ReflectionTestUtils.setField(variant, "id", 10L);
        inventory = new StoreInventory(store, variant);
        inventory.receive(5);
        quoteRequest = new CheckoutRequest(3L, 2L, null, PaymentMethod.COD, null,
                List.of(new CheckoutItemRequest(10L, 2)));

        when(addresses.findByIdAndUserId(3L, 7L)).thenReturn(Optional.of(address));
        when(stores.findByIdAndActiveTrue(2L)).thenReturn(Optional.of(store));
        when(variants.findById(10L)).thenReturn(Optional.of(variant));
        when(inventories.findByStoreIdAndVariantId(2L, 10L)).thenReturn(Optional.of(inventory));
        when(inventories.findLocked(2L, 10L)).thenReturn(Optional.of(inventory));
    }

    @Test
    void placesOrderAndClearsCartAfterSave() {
        CheckoutQuoteResponse quote = service.quote(user, quoteRequest);
        Cart cart = new Cart(user);
        cart.add(variant, 2);
        when(carts.findWithItemsByUserId(7L)).thenReturn(Optional.of(cart));
        when(orders.save(any())).thenAnswer(invocation -> {
            Object order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 50L);
            return order;
        });

        var response = service.place(user, placeRequest(quote.quoteToken(), null));

        assertThat(response.id()).isEqualTo(50L);
        assertThat(response.total()).isEqualByComparingTo("2030000.00");
        assertThat(inventory.getQuantity()).isEqualTo(3);
        assertThat(cart.getItems()).isEmpty();
        verify(carts).save(cart);
    }

    @Test
    void rejectsCheckoutWhenStoreInventoryIsInsufficient() {
        inventory.decrease(4);

        assertThatThrownBy(() -> service.quote(user, quoteRequest))
                .isInstanceOf(ApiException.class)
                .extracting("code").isEqualTo("INSUFFICIENT_STOCK");
    }

    @Test
    void rejectsInvalidCouponDuringQuote() {
        CheckoutRequest request = new CheckoutRequest(3L, 2L, "BAD", PaymentMethod.COD, null,
                quoteRequest.items());
        when(mockCouponRepository().findByCodeIgnoreCase("BAD")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.quote(user, request))
                .isInstanceOf(ApiException.class)
                .extracting("code").isEqualTo("COUPON_INVALID");
    }

    @Test
    void doesNotClearCartWhenOrderPersistenceFails() {
        CheckoutQuoteResponse quote = service.quote(user, quoteRequest);
        Cart cart = new Cart(user);
        cart.add(variant, 2);
        when(carts.findWithItemsByUserId(7L)).thenReturn(Optional.of(cart));
        when(orders.save(any())).thenThrow(new IllegalStateException("database failure"));

        assertThatThrownBy(() -> service.place(user, placeRequest(quote.quoteToken(), null)))
                .isInstanceOf(IllegalStateException.class);

        verify(carts, never()).save(any());
        assertThat(cart.getItemCount()).isEqualTo(2);
    }

    private PlaceOrderRequest placeRequest(String token, String couponCode) {
        return new PlaceOrderRequest(token, 3L, 2L, couponCode, PaymentMethod.COD, null,
                quoteRequest.items());
    }

    private CouponRepository mockCouponRepository() {
        return (CouponRepository) ReflectionTestUtils.getField(couponService, "couponRepository");
    }
}
