package com.utegear.dto.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record CheckoutQuoteResponse(
        String quoteToken,
        OffsetDateTime expiresAt,
        List<Item> items,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal shippingFee,
        BigDecimal total,
        CouponQuote coupon,
        StoreRef fulfillmentStore,
        List<String> warnings) {

    public record Item(Long variantId, String sku, String productName, BigDecimal unitPrice, int quantity,
                       BigDecimal subtotal, String imageUrl) {
    }

    public record StoreRef(Long id, String name) {
    }
}
