package com.utegear.controller;

import com.utegear.dto.cart.CartItemRequest;
import com.utegear.dto.cart.CartQuantityRequest;
import com.utegear.dto.cart.CartResponse;
import com.utegear.security.CustomUserDetails;
import com.utegear.service.CartApiService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartApiService cartService;

    public CartController(CartApiService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse get(@AuthenticationPrincipal CustomUserDetails principal) {
        return cartService.get(principal.getUser());
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse add(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CartItemRequest request) {
        return cartService.add(principal.getUser(), request.variantId(), request.quantity());
    }

    @PutMapping("/items/{itemId}")
    public CartResponse update(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long itemId,
            @Valid @RequestBody CartQuantityRequest request) {
        return cartService.update(principal.getUser(), itemId, request.quantity());
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long itemId) {
        cartService.remove(principal.getUser(), itemId);
    }

    @DeleteMapping("/items")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@AuthenticationPrincipal CustomUserDetails principal) {
        cartService.clear(principal.getUser());
    }
}
