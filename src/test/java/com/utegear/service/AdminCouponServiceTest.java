package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.utegear.dto.admin.AdminDtos;
import com.utegear.entity.DiscountType;
import com.utegear.exception.ApiException;
import com.utegear.repository.CouponRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminCouponServiceTest {

    private AdminCouponService service;
    private OffsetDateTime start;

    @BeforeEach
    void setUp() {
        service = new AdminCouponService(mock(CouponRepository.class));
        start = OffsetDateTime.of(2026, 10, 15, 8, 0, 0, 0, ZoneOffset.UTC);
    }

    @Test
    void rejectsEndTimeBeforeOrEqualStartAsClientError() {
        AdminDtos.CouponRequest before = request(
                DiscountType.FIXED_AMOUNT, new BigDecimal("50000"), start.minusMinutes(1));
        assertThatThrownBy(() -> service.create(before))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");

        AdminDtos.CouponRequest equal = request(
                DiscountType.FIXED_AMOUNT, new BigDecimal("50000"), start);
        assertThatThrownBy(() -> service.create(equal))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void rejectsNonPositiveDiscountValue() {
        AdminDtos.CouponRequest zero = request(
                DiscountType.FIXED_AMOUNT, BigDecimal.ZERO, start.plusDays(1));
        assertThatThrownBy(() -> service.create(zero))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");

        AdminDtos.CouponRequest negative = request(
                DiscountType.FIXED_AMOUNT, new BigDecimal("-1000"), start.plusDays(1));
        assertThatThrownBy(() -> service.create(negative))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void rejectsPercentageAboveOneHundredAsClientError() {
        AdminDtos.CouponRequest request = request(
                DiscountType.PERCENTAGE, new BigDecimal("100.01"), start.plusDays(1));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");
    }

    private AdminDtos.CouponRequest request(
            DiscountType type, BigDecimal value, OffsetDateTime end) {
        return new AdminDtos.CouponRequest(
                "WELCOME", type, value, BigDecimal.ZERO, null, start, end, 10, true);
    }
}
