package com.utegear.service;

import com.utegear.dto.cart.CartResponse;
import com.utegear.entity.Cart;
import com.utegear.entity.User;
import com.utegear.repository.StoreInventoryRepository;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultCartApiService implements CartApiService {

    private final CartService cartService;
    private final StoreInventoryRepository inventoryRepository;

    public DefaultCartApiService(CartService cartService, StoreInventoryRepository inventoryRepository) {
        this.cartService = cartService;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional
    public CartResponse get(User user) {
        return response(() -> cartService.getCart(user));
    }

    @Override
    @Transactional
    public CartResponse add(User user, Long variantId, int quantity) {
        return response(() -> cartService.addItem(user, variantId, quantity));
    }

    @Override
    @Transactional
    public CartResponse update(User user, Long itemId, int quantity) {
        return response(() -> cartService.updateQuantity(user, itemId, quantity));
    }

    @Override
    @Transactional
    public void remove(User user, Long itemId) {
        cartService.removeItem(user, itemId);
    }

    @Override
    @Transactional
    public void clear(User user) {
        cartService.clear(user);
    }

    private CartResponse response(Supplier<Cart> operation) {
        Cart cart = operation.get();
        var variantIds = cart.getItems().stream()
                .map(item -> item.getVariant().getId())
                .toList();
        Map<Long, Long> stockByVariant = variantIds.isEmpty()
                ? Map.of()
                : inventoryRepository.sumStockByVariantIds(variantIds)
                        .stream()
                        .collect(Collectors.toMap(
                                StoreInventoryRepository.VariantStock::getVariantId,
                                StoreInventoryRepository.VariantStock::getQuantity));
        return CartResponse.from(cart, stockByVariant);
    }
}
