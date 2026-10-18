package com.utegear.service;

import com.utegear.entity.Cart;
import com.utegear.entity.CartItem;
import com.utegear.entity.ProductStatus;
import com.utegear.entity.ProductVariant;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.CartRepository;
import com.utegear.repository.ProductVariantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductVariantRepository variantRepository;

    public CartService(
            CartRepository cartRepository,
            ProductVariantRepository variantRepository) {
        this.cartRepository = cartRepository;
        this.variantRepository = variantRepository;
    }

    @Transactional
    public Cart getCart(User user) {
        requirePersistedUser(user);
        return cartRepository.findWithItemsByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    @Transactional
    public Cart addItem(User user, Long variantId, int quantity) {
        requirePersistedUser(user);
        requirePositive(quantity);
        ProductVariant variant = requireAvailableVariant(variantId);
        Cart cart = cartRepository.findWithItemsByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(new Cart(user)));
        cart.add(variant, quantity);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateQuantity(User user, Long itemId, int quantity) {
        requirePersistedUser(user);
        requirePositive(quantity);
        Cart cart = requireCart(user.getId());
        CartItem item = requireOwnedItem(cart, itemId);
        cart.changeQuantity(item, quantity);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItem(User user, Long itemId) {
        requirePersistedUser(user);
        Cart cart = requireCart(user.getId());
        CartItem item = requireOwnedItem(cart, itemId);
        cart.remove(item);
        return cartRepository.save(cart);
    }

    @Transactional
    public void clear(User user) {
        requirePersistedUser(user);
        cartRepository.findWithItemsByUserId(user.getId()).ifPresent(cart -> {
            cart.clear();
            cartRepository.save(cart);
        });
    }

    private Cart requireCart(Long userId) {
        return cartRepository.findWithItemsByUserId(userId).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND, "CART_ITEM_NOT_FOUND", "Không tìm thấy sản phẩm trong giỏ hàng"));
    }

    private CartItem requireOwnedItem(Cart cart, Long itemId) {
        if (itemId == null || itemId <= 0) {
            throw validation("ID dòng giỏ hàng phải là số dương");
        }
        return cart.getItems().stream()
                .filter(item -> itemId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "CART_ITEM_NOT_FOUND", "Không tìm thấy sản phẩm trong giỏ hàng"));
    }

    private ProductVariant requireAvailableVariant(Long variantId) {
        if (variantId == null || variantId <= 0) {
            throw validation("ID biến thể phải là số dương");
        }
        ProductVariant variant = variantRepository.findById(variantId).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND, "VARIANT_NOT_FOUND", "Không tìm thấy biến thể sản phẩm"));
        if (!variant.isActive()
                || variant.getProduct() == null
                || variant.getProduct().getStatus() != ProductStatus.ACTIVE
                || !variant.getProduct().getVariants().contains(variant)) {
            throw new ApiException(
                    HttpStatus.CONFLICT, "VARIANT_UNAVAILABLE", "Biến thể không còn khả dụng cho sản phẩm");
        }
        return variant;
    }

    private void requirePersistedUser(User user) {
        if (user == null || user.getId() == null || user.getId() <= 0) {
            throw validation("Người dùng hợp lệ là bắt buộc");
        }
    }

    private void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw validation("Số lượng phải lớn hơn 0");
        }
        if (quantity > 99) {
            throw validation("Số lượng không được vượt quá 99");
        }
    }

    private ApiException validation(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
    }
}
