package com.utegear.dto.realtime;

import java.math.BigDecimal;

public record OrderCreatedEvent(Long orderId, String orderCode, String status, BigDecimal total, Long storeId) {
}
