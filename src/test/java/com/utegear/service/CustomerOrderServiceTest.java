package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.entity.Address;
import com.utegear.entity.Notification;
import com.utegear.entity.Order;
import com.utegear.entity.OrderStatus;
import com.utegear.entity.ShippingMethod;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.OrderRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

class CustomerOrderServiceTest {

    private OrderRepository orders;
    private NotificationService notifications;
    private OrderRealtimePublisher realtime;
    private InventoryService inventoryService;
    private CustomerOrderService service;
    private User user;

    @BeforeEach
    void setUp() {
        orders = mock(OrderRepository.class);
        notifications = mock(NotificationService.class);
        realtime = mock(OrderRealtimePublisher.class);
        inventoryService = mock(InventoryService.class);
        service = new CustomerOrderService(orders, notifications, realtime, inventoryService);
        user = new User("Nguyễn Văn An", "an@example.test", null, "hash");
        ReflectionTestUtils.setField(user, "id", 7L);
    }

    @Test
    void listsOnlyCurrentUsersOrders() {
        when(orders.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of()));

        assertThat(service.list(user, null, 0, 20).content()).isEmpty();
    }

    @Test
    void rejectsOrderOwnedByAnotherUserWithoutLeakingIt() {
        when(orders.findDetailedByIdAndUserId(99L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detail(user, 99L))
                .isInstanceOf(ApiException.class)
                .extracting("code").isEqualTo("ORDER_NOT_FOUND");
    }

    @Test
    void customerCanCancelOwnedPendingOrder() {
        Order order = pendingOrder();
        Notification notification = mock(Notification.class);
        when(orders.findLockedByIdAndUserId(10L, 7L)).thenReturn(Optional.of(order));
        when(orders.save(order)).thenReturn(order);
        when(notifications.create(any(), any(), any(), any(), any())).thenReturn(notification);

        service.cancel(user, 10L, "Không còn nhu cầu");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(inventoryService).restoreReservedStock(order);
        verify(realtime).publishStatusChangedAfterCommit(order, OrderStatus.PENDING, notification);
    }

    private Order pendingOrder() {
        Address address = new Address(user, "Nguyễn Văn An", "0900000001", "01 Võ Văn Ngân",
                "Linh Chiểu", "Thủ Đức", "TP.HCM", "Nhà");
        Order order = new Order("UTE-TEST-1", user, null, null, address, ShippingMethod.HOME_DELIVERY, null);
        ReflectionTestUtils.setField(order, "id", 10L);
        return order;
    }
}
