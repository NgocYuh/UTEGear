package com.utegear.service;

import com.utegear.dto.order.CouponQuote;
import com.utegear.entity.Coupon;
import com.utegear.exception.ApiException;
import com.utegear.repository.CouponRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional(readOnly = true)
    public CouponQuote validate(String code, BigDecimal subtotal) {
        Coupon coupon = find(code);
        BigDecimal discount = coupon.calculateDiscount(subtotal, OffsetDateTime.now(ZoneOffset.UTC));
        return CouponQuote.from(coupon, subtotal, discount);
    }

    @Transactional
    public CouponQuote use(String code, BigDecimal subtotal) {
        String normalizedCode = normalize(code);
        Coupon coupon = couponRepository.findLockedByCode(normalizedCode).orElseThrow(this::invalidCode);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        BigDecimal discount = coupon.calculateDiscount(subtotal, now);
        coupon.use(subtotal, now);
        couponRepository.save(coupon);
        return CouponQuote.from(coupon, subtotal, discount);
    }

    private Coupon find(String code) {
        return couponRepository.findByCodeIgnoreCase(normalize(code)).orElseThrow(this::invalidCode);
    }

    private String normalize(String code) {
        if (code == null || code.isBlank()) {
            throw invalidCode();
        }
        return code.trim().toUpperCase();
    }

    private ApiException invalidCode() {
        return new ApiException(HttpStatus.CONFLICT, "COUPON_INVALID", "Mã giảm giá không hợp lệ");
    }
}
