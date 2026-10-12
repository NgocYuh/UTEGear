package com.utegear.dto.realtime;

public record OrderStatusChangedEvent(Long orderId, String orderCode, String previousStatus, String status) {
}
