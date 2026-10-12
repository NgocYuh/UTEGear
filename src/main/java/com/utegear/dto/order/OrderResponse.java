package com.utegear.dto.order;

import com.utegear.entity.Order;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderCode,
        String status,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal shippingFee,
        BigDecimal total,
        String paymentMethod,
        String paymentStatus,
        OffsetDateTime createdAt,
        List<Item> items) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(), order.getOrderCode(), order.getStatus().name(), order.getSubtotal(),
                order.getDiscount(), order.getShippingFee(), order.getTotal(),
                order.getPayment().getMethod().name(), order.getPayment().getStatus().name(), order.getCreatedAt(),
                order.getItems().stream().map(item -> new Item(
                        item.getVariant().getId(), item.getSku(), item.getProductName(), item.getUnitPrice(),
                        item.getQuantity(), item.getSubtotal(), item.getImageUrl())).toList());
    }

    public record Item(Long variantId, String sku, String productName, BigDecimal unitPrice, int quantity,
                       BigDecimal subtotal, String imageUrl) {
    }
}
