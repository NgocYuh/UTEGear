package com.utegear.controller;

import com.utegear.dto.admin.AdminDtos;
import com.utegear.service.AdminCouponService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/coupons")
public class AdminCouponController {
    private final AdminCouponService service;

    public AdminCouponController(AdminCouponService service) { this.service = service; }

    @GetMapping
    public List<AdminDtos.CouponView> findAll() { return service.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminDtos.CouponView create(@Valid @RequestBody AdminDtos.CouponRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public AdminDtos.CouponView update(
            @PathVariable Long id, @Valid @RequestBody AdminDtos.CouponRequest request) {
        return service.update(id, request);
    }
}
