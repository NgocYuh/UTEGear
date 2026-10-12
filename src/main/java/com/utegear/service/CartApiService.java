package com.utegear.service;

import com.utegear.dto.cart.CartResponse;
import com.utegear.entity.User;

public interface CartApiService {

    CartResponse get(User user);

    CartResponse add(User user, Long variantId, int quantity);

    CartResponse update(User user, Long itemId, int quantity);

    void remove(User user, Long itemId);

    void clear(User user);
}
