package com.utegear.entity;

import com.utegear.exception.ApiException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import org.springframework.http.HttpStatus;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "minimum_order_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal minimumOrderValue;

    @Column(name = "maximum_discount", precision = 12, scale = 2)
    private BigDecimal maximumDiscount;

    @Column(name = "valid_from", nullable = false)
    private OffsetDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    private OffsetDateTime validUntil;

    @Column(name = "max_uses", nullable = false)
    private int maxUses;

    @Column(name = "used_count", nullable = false)
    private int usedCount;

    @Column(nullable = false)
    private boolean active = true;

    protected Coupon() {
    }

    public Coupon(
            String code,
            DiscountType discountType,
            BigDecimal discountValue,
            BigDecimal minimumOrderValue,
            BigDecimal maximumDiscount,
            OffsetDateTime validFrom,
            OffsetDateTime validUntil,
            int maxUses) {
        this.code = requireCode(code);
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minimumOrderValue = minimumOrderValue == null ? BigDecimal.ZERO : minimumOrderValue;
        this.maximumDiscount = maximumDiscount;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.maxUses = maxUses;
        validateDefinition();
    }

    public BigDecimal calculateDiscount(BigDecimal subtotal, OffsetDateTime now) {
        validateFor(subtotal, now);
        BigDecimal discount = discountType == DiscountType.PERCENTAGE
                ? subtotal.multiply(discountValue).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : discountValue;
        if (maximumDiscount != null && discount.compareTo(maximumDiscount) > 0) {
            discount = maximumDiscount;
        }
        return discount.min(subtotal).setScale(2, RoundingMode.HALF_UP);
    }

    public void use(BigDecimal subtotal, OffsetDateTime now) {
        calculateDiscount(subtotal, now);
        usedCount++;
    }

    public void update(
            String code,
            DiscountType discountType,
            BigDecimal discountValue,
            BigDecimal minimumOrderValue,
            BigDecimal maximumDiscount,
            OffsetDateTime validFrom,
            OffsetDateTime validUntil,
            int maxUses,
            boolean active) {
        this.code = requireCode(code);
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minimumOrderValue = minimumOrderValue == null ? BigDecimal.ZERO : minimumOrderValue;
        this.maximumDiscount = maximumDiscount;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.maxUses = maxUses;
        this.active = active;
        validateDefinition();
    }

    private void validateFor(BigDecimal subtotal, OffsetDateTime now) {
        if (subtotal == null || subtotal.signum() < 0 || now == null) {
            throw invalid("Giá trị đơn hàng hoặc thời điểm không hợp lệ");
        }
        if (!active || now.isBefore(validFrom) || now.isAfter(validUntil)) {
            throw invalid("Mã giảm giá không còn hiệu lực");
        }
        if (maxUses > 0 && usedCount >= maxUses) {
            throw invalid("Mã giảm giá đã hết lượt sử dụng");
        }
        if (subtotal.compareTo(minimumOrderValue) < 0) {
            throw invalid("Đơn hàng chưa đạt giá trị tối thiểu");
        }
    }

    private void validateDefinition() {
        if (discountType == null || discountValue == null || discountValue.signum() < 0) {
            throw new IllegalArgumentException("Giá trị giảm giá không hợp lệ");
        }
        if (discountType == DiscountType.PERCENTAGE && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Phần trăm giảm giá không được vượt quá 100");
        }
        if (minimumOrderValue.signum() < 0 || maximumDiscount != null && maximumDiscount.signum() < 0) {
            throw new IllegalArgumentException("Giới hạn giá trị đơn hàng không hợp lệ");
        }
        if (validFrom == null || validUntil == null || !validUntil.isAfter(validFrom)) {
            throw new IllegalArgumentException("Thời hạn mã giảm giá không hợp lệ");
        }
        if (maxUses < 0) {
            throw new IllegalArgumentException("Số lượt sử dụng không hợp lệ");
        }
    }

    private String requireCode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Mã giảm giá không được để trống");
        }
        return value.trim().toUpperCase();
    }

    private ApiException invalid(String message) {
        return new ApiException(HttpStatus.CONFLICT, "COUPON_INVALID", message);
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public DiscountType getDiscountType() { return discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public BigDecimal getMinimumOrderValue() { return minimumOrderValue; }
    public BigDecimal getMaximumDiscount() { return maximumDiscount; }
    public OffsetDateTime getValidFrom() { return validFrom; }
    public OffsetDateTime getValidUntil() { return validUntil; }
    public int getMaxUses() { return maxUses; }
    public int getUsedCount() { return usedCount; }
    public boolean isActive() { return active; }
}
