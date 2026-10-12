package com.utegear.service;

import com.utegear.entity.Cart;
import com.utegear.entity.CartItem;
import com.utegear.entity.ProductStatus;
import com.utegear.entity.ProductVariant;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.CartItemRepository;
import com.utegear.repository.CartRepository;
import com.utegear.repository.ProductVariantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductVariantRepository variantRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
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
        CartItem item = requireOwnedItem(user.getId(), itemId);
        Cart cart = item.getCart();
        cart.changeQuantity(item, quantity);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItem(User user, Long itemId) {
        requirePersistedUser(user);
        CartItem item = requireOwnedItem(user.getId(), itemId);
        Cart cart = item.getCart();
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

    private CartItem requireOwnedItem(Long userId, Long itemId) {
        if (itemId == null || itemId <= 0) {
            throw validation("ID dòng giỏ hàng phải là số dương");
        }
        return cartItemRepository.findByIdAndCartUserId(itemId, userId).orElseThrow(() -> new ApiException(
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
    }

    private ApiException validation(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
    }
}
