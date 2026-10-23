package com.utegear.dto.payment;

import java.math.BigDecimal;

public record VNPayCallbackResult(
        boolean success,
        String orderCode,
        String transactionNo,
        BigDecimal amount,
        String bankCode,
        String responseCode,
        String message
) {
}

