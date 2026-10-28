package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.utegear.dto.admin.AdminDtos;
import com.utegear.entity.Category;
import com.utegear.entity.OrderStatus;
import com.utegear.entity.Product;
import com.utegear.entity.ProductStatus;
import com.utegear.entity.ProductVariant;
import com.utegear.repository.BrandRepository;
import com.utegear.repository.CategoryRepository;
import com.utegear.repository.CouponRepository;
import com.utegear.repository.OrderRepository;
import com.utegear.repository.ProductRepository;
import com.utegear.repository.StoreRepository;
import com.utegear.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AdminDashboardServiceTest {

    private OrderRepository orders;
    private ProductRepository products;
    private StoreRepository stores;
    private UserRepository users;
    private CategoryRepository categories;
    private BrandRepository brands;
    private CouponRepository coupons;
    private AdminDashboardService service;

    @BeforeEach
    void setUp() {
        orders = mock(OrderRepository.class);
        products = mock(ProductRepository.class);
        stores = mock(StoreRepository.class);
        users = mock(UserRepository.class);
        categories = mock(CategoryRepository.class);
        brands = mock(BrandRepository.class);
        coupons = mock(CouponRepository.class);

        service = new AdminDashboardService(orders, products, stores, users, categories, brands, coupons);
    }

    @Test
    void summaryComputesAllMetricsIncludingRevenueAndTopProducts() {
        when(orders.count()).thenReturn(50L);
        when(orders.countByStatus(OrderStatus.PENDING)).thenReturn(5L);
        when(orders.countByStatus(OrderStatus.COMPLETED)).thenReturn(40L);
        when(orders.countByStatus(OrderStatus.CANCELLED)).thenReturn(5L);
        when(orders.sumTotalRevenue()).thenReturn(new BigDecimal("125000000.00"));

        when(products.count()).thenReturn(100L);
        when(products.countByStatus(ProductStatus.ACTIVE)).thenReturn(90L);
        when(stores.count()).thenReturn(3L);
        when(users.count()).thenReturn(200L);
        when(categories.count()).thenReturn(6L);
        when(brands.count()).thenReturn(10L);
        when(coupons.count()).thenReturn(4L);

        Category cat = new Category("Bàn phím", "ban-phim", null, 0);
        Product p = new Product("Bàn phím cơ AKKO", "akko-v3", cat, null, new BigDecimal("1500000"), null);
        ProductVariant variant = new ProductVariant("AKKO-DEFAULT", Map.of(), true, BigDecimal.ZERO);
        p.addVariant(variant);
        ReflectionTestUtils.setField(p, "id", 1L);
        ReflectionTestUtils.setField(p, "soldCount", 85);

        when(products.findTopSelling(eq(ProductStatus.ACTIVE), any())).thenReturn(List.of(p));

        AdminDtos.DashboardSummary summary = service.summary();

        assertThat(summary.totalOrders()).isEqualTo(50L);
        assertThat(summary.pendingOrders()).isEqualTo(5L);
        assertThat(summary.completedOrders()).isEqualTo(40L);
        assertThat(summary.cancelledOrders()).isEqualTo(5L);
        assertThat(summary.totalRevenue()).isEqualByComparingTo("125000000.00");
        assertThat(summary.totalProducts()).isEqualTo(100L);
        assertThat(summary.topProducts()).hasSize(1);
        assertThat(summary.topProducts().get(0).name()).isEqualTo("Bàn phím cơ AKKO");
        assertThat(summary.topProducts().get(0).soldCount()).isEqualTo(85);
    }
}

