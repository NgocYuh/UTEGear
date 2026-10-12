package com.utegear.controller;

import com.utegear.dto.order.AdminOrderDtos;
import com.utegear.entity.OrderStatus;
import com.utegear.service.AdminOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminOrderService service;

    public AdminOrderController(AdminOrderService service) {
        this.service = service;
    }

    @GetMapping
    public AdminOrderDtos.OrderPage list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.list(q, status, storeId, page, size);
    }

    @GetMapping("/{orderId}")
    public AdminOrderDtos.Detail detail(@PathVariable Long orderId) {
        return service.detail(orderId);
    }

    @PutMapping("/{orderId}/status")
    public AdminOrderDtos.Detail updateStatus(@PathVariable Long orderId,
            @Valid @RequestBody AdminOrderDtos.StatusRequest request) {
        return service.updateStatus(orderId, request.status());
    }
}
