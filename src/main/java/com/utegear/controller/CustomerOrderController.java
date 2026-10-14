package com.utegear.controller;

import com.utegear.dto.order.AdminOrderDtos;
import com.utegear.dto.order.CustomerOrderDtos;
import com.utegear.entity.OrderStatus;
import com.utegear.security.CustomUserDetails;
import com.utegear.service.CustomerOrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class CustomerOrderController {

    private final CustomerOrderService orderService;

    public CustomerOrderController(CustomerOrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public CustomerOrderDtos.OrderPage list(@AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return orderService.list(principal.getUser(), status, page, size);
    }

    @GetMapping("/{orderId}")
    public AdminOrderDtos.Detail detail(@AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long orderId) {
        return orderService.detail(principal.getUser(), orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public AdminOrderDtos.Detail cancel(@AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long orderId, @Valid @RequestBody CustomerOrderDtos.CancelRequest request) {
        return orderService.cancel(principal.getUser(), orderId, request.reason());
    }
}
