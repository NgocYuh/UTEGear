package com.utegear.service;

import com.utegear.dto.admin.AdminDtos;
import com.utegear.entity.OrderStatus;
import com.utegear.entity.ProductStatus;
import com.utegear.repository.BrandRepository;
import com.utegear.repository.CategoryRepository;
import com.utegear.repository.CouponRepository;
import com.utegear.repository.OrderRepository;
import com.utegear.repository.ProductRepository;
import com.utegear.repository.StoreRepository;
import com.utegear.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDashboardService {
    private final OrderRepository orders;
    private final ProductRepository products;
    private final StoreRepository stores;
    private final UserRepository users;
    private final CategoryRepository categories;
    private final BrandRepository brands;
    private final CouponRepository coupons;

    public AdminDashboardService(
            OrderRepository orders, ProductRepository products, StoreRepository stores,
            UserRepository users, CategoryRepository categories, BrandRepository brands,
            CouponRepository coupons) {
        this.orders = orders;
        this.products = products;
        this.stores = stores;
        this.users = users;
        this.categories = categories;
        this.brands = brands;
        this.coupons = coupons;
    }

    @Transactional(readOnly = true)
    public AdminDtos.DashboardSummary summary() {
        return new AdminDtos.DashboardSummary(
                orders.count(), orders.countByStatus(OrderStatus.PENDING), products.count(),
                products.countByStatus(ProductStatus.ACTIVE), stores.count(), users.count(),
                categories.count(), brands.count(), coupons.count());
    }
}
