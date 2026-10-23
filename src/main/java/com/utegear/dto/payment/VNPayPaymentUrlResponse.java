package com.utegear.dto.payment;

import java.math.BigDecimal;

public record VNPayPaymentUrlResponse(
        String paymentUrl,
        String orderCode,
        BigDecimal amount
) {
}

