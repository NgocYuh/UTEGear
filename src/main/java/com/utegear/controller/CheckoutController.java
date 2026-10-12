package com.utegear.controller;

import com.utegear.dto.order.CheckoutQuoteResponse;
import com.utegear.dto.order.CheckoutRequest;
import com.utegear.dto.order.OrderResponse;
import com.utegear.dto.order.PlaceOrderRequest;
import com.utegear.security.CustomUserDetails;
import com.utegear.service.CheckoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/checkout/quote")
    public CheckoutQuoteResponse quote(@AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CheckoutRequest request) {
        return checkoutService.quote(principal.getUser(), request);
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse place(@AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody PlaceOrderRequest request) {
        return checkoutService.place(principal.getUser(), request);
    }
}
