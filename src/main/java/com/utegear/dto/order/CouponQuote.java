package com.utegear.dto.order;

import com.utegear.entity.Coupon;
import com.utegear.entity.DiscountType;
import java.math.BigDecimal;

public record CouponQuote(
        String code,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal discountAmount,
        BigDecimal minimumOrderValue,
        BigDecimal subtotalAfterDiscount,
        boolean valid) {

    public static CouponQuote from(Coupon coupon, BigDecimal subtotal, BigDecimal discountAmount) {
        return new CouponQuote(
                coupon.getCode(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                discountAmount,
                coupon.getMinimumOrderValue(),
                subtotal.subtract(discountAmount),
                true);
    }
}
