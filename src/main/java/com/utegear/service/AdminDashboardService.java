package com.utegear.service;

import com.utegear.dto.admin.AdminDtos;
import com.utegear.entity.OrderStatus;
import com.utegear.entity.Product;
import com.utegear.entity.ProductStatus;
import com.utegear.repository.BrandRepository;
import com.utegear.repository.CategoryRepository;
import com.utegear.repository.CouponRepository;
import com.utegear.repository.OrderRepository;
import com.utegear.repository.ProductRepository;
import com.utegear.repository.StoreRepository;
import com.utegear.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.PageRequest;
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
        BigDecimal totalRevenue = orders.sumTotalRevenue();
        long completedOrders = orders.countByStatus(OrderStatus.COMPLETED);
        long cancelledOrders = orders.countByStatus(OrderStatus.CANCELLED);
        List<Product> topProductList = products.findTopSelling(ProductStatus.ACTIVE, PageRequest.of(0, 5));
        List<AdminDtos.TopProductSummary> topProducts = topProductList.stream().map(p -> new AdminDtos.TopProductSummary(
                p.getId(),
                p.getName(),
                p.getCategory() != null ? p.getCategory().getName() : "Khác",
                p.getEffectivePrice(),
                p.getSoldCount(),
                p.getImages() != null && !p.getImages().isEmpty() ? p.getImages().get(0).getSecureUrl() : "/images/placeholder.svg"
        )).toList();

        return new AdminDtos.DashboardSummary(
                orders.count(), orders.countByStatus(OrderStatus.PENDING), products.count(),
                products.countByStatus(ProductStatus.ACTIVE), stores.count(), users.count(),
                categories.count(), brands.count(), coupons.count(),
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                completedOrders,
                cancelledOrders,
                topProducts);
    }
}
