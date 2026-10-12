package com.utegear.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.utegear.exception.ApiException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CouponTest {

    private final OffsetDateTime now = OffsetDateTime.of(2026, 10, 12, 12, 0, 0, 0, ZoneOffset.UTC);

    @Test
    void rejectsExpiredCoupon() {
        Coupon coupon = coupon(
                DiscountType.FIXED_AMOUNT, "100000", "500000", null,
                now.minusDays(2), now.minusDays(1), 0);

        assertInvalid(() -> coupon.calculateDiscount(new BigDecimal("1000000"), now));
    }

    @Test
    void rejectsExhaustedCoupon() {
        Coupon coupon = coupon(
                DiscountType.FIXED_AMOUNT, "100000", "500000", null,
                now.minusDays(1), now.plusDays(1), 1);
        coupon.use(new BigDecimal("1000000"), now);

        assertInvalid(() -> coupon.calculateDiscount(new BigDecimal("1000000"), now));
    }

    @Test
    void rejectsSubtotalBelowMinimum() {
        Coupon coupon = coupon(
                DiscountType.FIXED_AMOUNT, "100000", "1000000", null,
                now.minusDays(1), now.plusDays(1), 0);

        assertInvalid(() -> coupon.calculateDiscount(new BigDecimal("999999"), now));
    }

    @Test
    void capsPercentageDiscountAtMaximum() {
        Coupon coupon = coupon(
                DiscountType.PERCENTAGE, "20", "500000", "150000",
                now.minusDays(1), now.plusDays(1), 0);

        assertThat(coupon.calculateDiscount(new BigDecimal("1000000"), now))
                .isEqualByComparingTo("150000.00");
    }

    @Test
    void fixedDiscountCannotMakeSubtotalNegative() {
        Coupon coupon = coupon(
                DiscountType.FIXED_AMOUNT, "500000", "0", null,
                now.minusDays(1), now.plusDays(1), 0);

        assertThat(coupon.calculateDiscount(new BigDecimal("300000"), now))
                .isEqualByComparingTo("300000.00");
    }

    private Coupon coupon(
            DiscountType type,
            String value,
            String minimum,
            String maximum,
            OffsetDateTime validFrom,
            OffsetDateTime validUntil,
            int maxUses) {
        return new Coupon(
                "ute-sale",
                type,
                new BigDecimal(value),
                new BigDecimal(minimum),
                maximum == null ? null : new BigDecimal(maximum),
                validFrom,
                validUntil,
                maxUses);
    }

    private void assertInvalid(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("COUPON_INVALID");
    }
}
