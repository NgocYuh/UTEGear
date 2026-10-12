package com.utegear.service;

import com.utegear.dto.notification.NotificationResponse;
import com.utegear.dto.realtime.OrderCreatedEvent;
import com.utegear.dto.realtime.OrderStatusChangedEvent;
import com.utegear.dto.realtime.RealtimeEvent;
import com.utegear.entity.Notification;
import com.utegear.entity.Order;
import com.utegear.entity.OrderStatus;
import com.utegear.util.TransactionCallbacks;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderRealtimePublisher {

    static final String ADMIN_ORDERS_DESTINATION = "/topic/admin/orders";
    static final String USER_ORDERS_DESTINATION = "/queue/orders";
    static final String USER_NOTIFICATIONS_DESTINATION = "/queue/notifications";

    private final SimpMessagingTemplate messaging;
    private final TransactionCallbacks transactionCallbacks;

    public OrderRealtimePublisher(SimpMessagingTemplate messaging, TransactionCallbacks transactionCallbacks) {
        this.messaging = messaging;
        this.transactionCallbacks = transactionCallbacks;
    }

    public void publishCreatedAfterCommit(Order order) {
        Long storeId = order.getFulfillmentStore() == null ? null : order.getFulfillmentStore().getId();
        RealtimeEvent<OrderCreatedEvent> event = envelope("ORDER_CREATED", new OrderCreatedEvent(
                order.getId(), order.getOrderCode(), order.getStatus().name(), order.getTotal(), storeId));
        transactionCallbacks.afterCommit(() -> messaging.convertAndSend(ADMIN_ORDERS_DESTINATION, event));
    }

    public void publishStatusChangedAfterCommit(Order order, OrderStatus previousStatus, Notification notification) {
        String username = order.getUser().getEmail();
        RealtimeEvent<OrderStatusChangedEvent> orderEvent = envelope("ORDER_STATUS_CHANGED",
                new OrderStatusChangedEvent(order.getId(), order.getOrderCode(), previousStatus.name(),
                        order.getStatus().name()));
        RealtimeEvent<NotificationResponse> notificationEvent = envelope(
                "NOTIFICATION_CREATED", NotificationResponse.from(notification));
        transactionCallbacks.afterCommit(() -> {
            messaging.convertAndSendToUser(username, USER_ORDERS_DESTINATION, orderEvent);
            messaging.convertAndSendToUser(username, USER_NOTIFICATIONS_DESTINATION, notificationEvent);
        });
    }

    private <T> RealtimeEvent<T> envelope(String type, T data) {
        return new RealtimeEvent<>("evt_" + UUID.randomUUID(), type, OffsetDateTime.now(ZoneOffset.UTC), data);
    }
}
