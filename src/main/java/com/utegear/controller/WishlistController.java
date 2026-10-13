package com.utegear.controller;

import com.utegear.dto.wishlist.WishlistItemRequest;
import com.utegear.dto.wishlist.WishlistItemResponse;
import com.utegear.security.CustomUserDetails;
import com.utegear.service.WishlistService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public List<WishlistItemResponse> list(@AuthenticationPrincipal CustomUserDetails principal) {
        return wishlistService.list(principal.getUser());
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistItemResponse add(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody WishlistItemRequest request) {
        return wishlistService.add(principal.getUser(), request.productId());
    }

    @DeleteMapping("/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long productId) {
        wishlistService.remove(principal.getUser(), productId);
    }
}
