package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.dto.realtime.OrderStatusChangedEvent;
import com.utegear.dto.realtime.RealtimeEvent;
import com.utegear.entity.Notification;
import com.utegear.entity.NotificationType;
import com.utegear.entity.Order;
import com.utegear.entity.OrderStatus;
import com.utegear.entity.Store;
import com.utegear.entity.User;
import com.utegear.util.TransactionCallbacks;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class OrderRealtimePublisherTest {

    private SimpMessagingTemplate messaging;
    private OrderRealtimePublisher publisher;
    private Order order;
    private User user;

    @BeforeEach
    void setUp() {
        messaging = mock(SimpMessagingTemplate.class);
        publisher = new OrderRealtimePublisher(messaging, new TransactionCallbacks());
        order = mock(Order.class);
        user = new User("Nguyễn Văn An", "owner@example.test", null, "hash");
        ReflectionTestUtils.setField(user, "id", 7L);
        Store store = mock(Store.class);
        when(store.getId()).thenReturn(11L);
        when(order.getId()).thenReturn(9001L);
        when(order.getOrderCode()).thenReturn("UTE-0001");
        when(order.getStatus()).thenReturn(OrderStatus.PROCESSING);
        when(order.getTotal()).thenReturn(new BigDecimal("2710000"));
        when(order.getFulfillmentStore()).thenReturn(store);
        when(order.getUser()).thenReturn(user);
    }

    @Test
    void publishesCreatedOrderOnlyToAdminTopic() {
        publisher.publishCreatedAfterCommit(order);

        ArgumentCaptor<RealtimeEvent<?>> event = eventCaptor();
        verify(messaging).convertAndSend(eq("/topic/admin/orders"), event.capture());
        assertThat(event.getValue().type()).isEqualTo("ORDER_CREATED");
    }

    @Test
    void publishesStatusAndNotificationOnlyToOrderOwner() {
        Notification notification = new Notification(user, NotificationType.ORDER_STATUS_CHANGED,
                "Đơn hàng đang xử lý", "Đơn UTE-0001 đang được xử lý.", "9001");

        publisher.publishStatusChangedAfterCommit(order, OrderStatus.CONFIRMED, notification);

        ArgumentCaptor<RealtimeEvent<?>> orderEvent = eventCaptor();
        verify(messaging).convertAndSendToUser(eq("owner@example.test"), eq("/queue/orders"),
                orderEvent.capture());
        verify(messaging).convertAndSendToUser(eq("owner@example.test"), eq("/queue/notifications"),
                org.mockito.ArgumentMatchers.any());
        assertThat(orderEvent.getValue().data()).isInstanceOf(OrderStatusChangedEvent.class);
        OrderStatusChangedEvent payload = (OrderStatusChangedEvent) orderEvent.getValue().data();
        assertThat(payload.previousStatus()).isEqualTo("CONFIRMED");
        assertThat(payload.status()).isEqualTo("PROCESSING");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ArgumentCaptor<RealtimeEvent<?>> eventCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(RealtimeEvent.class);
    }
}
