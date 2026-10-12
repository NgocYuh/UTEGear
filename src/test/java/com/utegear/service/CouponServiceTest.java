package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.dto.order.CouponQuote;
import com.utegear.entity.Coupon;
import com.utegear.entity.DiscountType;
import com.utegear.repository.CouponRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CouponServiceTest {

    @Test
    void locksAndConsumesLimitedCoupon() {
        CouponRepository repository = mock(CouponRepository.class);
        CouponService service = new CouponService(repository);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Coupon coupon = new Coupon(
                "UTE100",
                DiscountType.FIXED_AMOUNT,
                new BigDecimal("100000"),
                new BigDecimal("500000"),
                null,
                now.minusDays(1),
                now.plusDays(1),
                10);
        when(repository.findLockedByCode("UTE100")).thenReturn(Optional.of(coupon));

        CouponQuote quote = service.use(" ute100 ", new BigDecimal("1000000"));

        assertThat(quote.discountAmount()).isEqualByComparingTo("100000.00");
        assertThat(quote.subtotalAfterDiscount()).isEqualByComparingTo("900000.00");
        assertThat(coupon.getUsedCount()).isEqualTo(1);
        verify(repository).save(coupon);
    }
}
