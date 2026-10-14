package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.entity.Address;
import com.utegear.entity.Category;
import com.utegear.entity.Order;
import com.utegear.entity.Notification;
import com.utegear.entity.OrderStatus;
import com.utegear.entity.PaymentMethod;
import com.utegear.entity.Product;
import com.utegear.entity.ProductVariant;
import com.utegear.entity.ShippingMethod;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class AdminOrderServiceTest {

    private OrderRepository orders;
    private AdminOrderService service;
    private NotificationService notificationService;
    private OrderRealtimePublisher realtimePublisher;
    private InventoryService inventoryService;
    private Order order;

    @BeforeEach
    void setUp() {
        orders = mock(OrderRepository.class);
        notificationService = mock(NotificationService.class);
        realtimePublisher = mock(OrderRealtimePublisher.class);
        inventoryService = mock(InventoryService.class);
        service = new AdminOrderService(orders, notificationService, realtimePublisher, inventoryService);
        User user = new User("Nguyễn Văn An", "an@example.test", null, "hash");
        ReflectionTestUtils.setField(user, "id", 7L);
        Address address = new Address(user, "Nguyễn Văn An", "0900000001", "01 Võ Văn Ngân",
                "Linh Chiểu", "Thủ Đức", "TP.HCM", "Nhà");
        Product product = new Product("Chuột M1", "chuot-m1",
                new Category("Chuột", "chuot", null, 0), null, new BigDecimal("500000"), null);
        ProductVariant variant = new ProductVariant("M1-DEFAULT", Map.of(), true, BigDecimal.ZERO);
        product.addVariant(variant);
        ReflectionTestUtils.setField(variant, "id", 10L);
        order = new Order("UTE-0001", user, null, null, address, ShippingMethod.HOME_DELIVERY, null);
        order.addItem(variant, 1, null);
        order.createPayment(PaymentMethod.COD);
        ReflectionTestUtils.setField(order, "id", 20L);
        when(orders.findLockedById(20L)).thenReturn(Optional.of(order));
        when(orders.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.create(any(), any(), any(), any(), any()))
                .thenAnswer(invocation -> new Notification(invocation.getArgument(0), invocation.getArgument(1),
                        invocation.getArgument(2), invocation.getArgument(3), invocation.getArgument(4)));
    }

    @Test
    void confirmsPendingOrder() {
        var response = service.updateStatus(20L, OrderStatus.CONFIRMED);

        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.confirmedAt()).isNotNull();
        verify(orders).save(order);
    }

    @Test
    void rejectsSkippedStatusTransition() {
        assertThatThrownBy(() -> service.updateStatus(20L, OrderStatus.SHIPPING))
                .isInstanceOf(ApiException.class)
                .extracting("code").isEqualTo("INVALID_ORDER_TRANSITION");

        verify(orders, never()).save(any());
    }

    @Test
    void cancellingOrderAlsoCancelsPendingPayment() {
        var response = service.updateStatus(20L, OrderStatus.CANCELLED);

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(response.payment().status()).isEqualTo("CANCELLED");
        verify(inventoryService).restoreReservedStock(order);
    }

    @Test
    void completingCodOrderMarksPaymentPaid() {
        order.transitionTo(OrderStatus.CONFIRMED);
        order.transitionTo(OrderStatus.PROCESSING);
        order.transitionTo(OrderStatus.SHIPPING);

        var response = service.updateStatus(20L, OrderStatus.COMPLETED);

        assertThat(response.status()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(response.payment().status()).isEqualTo("PAID");
        assertThat(order.getPayment().getPaidAt()).isNotNull();
    }

    @Test
    void listsFilteredOrdersWithPaginationMetadata() {
        when(orders.findAll(org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Order>>any(),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(order)));

        var response = service.list("UTE-0001", OrderStatus.PENDING, null, 0, 20);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().orderCode()).isEqualTo("UTE-0001");
        assertThat(response.totalElements()).isEqualTo(1);
    }

    @Test
    void returnsDetailedOrder() {
        when(orders.findDetailedById(20L)).thenReturn(Optional.of(order));

        var response = service.detail(20L);

        assertThat(response.id()).isEqualTo(20L);
        assertThat(response.items()).hasSize(1);
        assertThat(response.payment().method()).isEqualTo("COD");
    }
}
