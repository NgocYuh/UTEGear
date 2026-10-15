package com.utegear.service;

import com.utegear.dto.admin.AdminDtos;
import com.utegear.entity.Coupon;
import com.utegear.exception.ApiException;
import com.utegear.repository.CouponRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminCouponService {
    private final CouponRepository repository;

    public AdminCouponService(CouponRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AdminDtos.CouponView> findAll() {
        return repository.findAll().stream().map(AdminDtos.CouponView::from).toList();
    }

    @Transactional
    public AdminDtos.CouponView create(AdminDtos.CouponRequest request) {
        validate(request);
        if (repository.findByCodeIgnoreCase(request.code().trim()).isPresent()) {
            throw duplicate();
        }
        Coupon coupon = new Coupon(request.code(), request.discountType(), request.discountValue(),
                request.minimumOrderValue(), request.maximumDiscount(), request.validFrom(),
                request.validUntil(), request.maxUses());
        coupon.update(request.code(), request.discountType(), request.discountValue(),
                request.minimumOrderValue(), request.maximumDiscount(), request.validFrom(),
                request.validUntil(), request.maxUses(), request.active());
        return AdminDtos.CouponView.from(save(coupon));
    }

    @Transactional
    public AdminDtos.CouponView update(Long id, AdminDtos.CouponRequest request) {
        validate(request);
        Coupon coupon = repository.findById(id).orElseThrow(this::notFound);
        if (repository.existsByCodeIgnoreCaseAndIdNot(request.code().trim(), id)) {
            throw duplicate();
        }
        coupon.update(request.code(), request.discountType(), request.discountValue(),
                request.minimumOrderValue(), request.maximumDiscount(), request.validFrom(),
                request.validUntil(), request.maxUses(), request.active());
        return AdminDtos.CouponView.from(save(coupon));
    }

    private Coupon save(Coupon coupon) {
        try { return repository.save(coupon); }
        catch (DataIntegrityViolationException exception) { throw duplicate(); }
    }

    private void validate(AdminDtos.CouponRequest request) {
        if (!request.validUntil().isAfter(request.validFrom())) {
            throw invalid("Thời điểm kết thúc phải sau thời điểm bắt đầu");
        }
        if (request.discountType() == com.utegear.entity.DiscountType.PERCENTAGE
                && request.discountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw invalid("Phần trăm giảm giá không được vượt quá 100");
        }
    }

    private ApiException duplicate() { return new ApiException(HttpStatus.CONFLICT, "COUPON_ALREADY_EXISTS", "Mã giảm giá đã tồn tại"); }
    private ApiException notFound() { return new ApiException(HttpStatus.NOT_FOUND, "COUPON_NOT_FOUND", "Không tìm thấy mã giảm giá"); }
    private ApiException invalid(String message) { return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message); }
}
