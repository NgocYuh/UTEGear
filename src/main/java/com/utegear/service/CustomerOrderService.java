package com.utegear.service;

import com.utegear.dto.order.AdminOrderDtos;
import com.utegear.dto.order.CustomerOrderDtos;
import com.utegear.entity.Notification;
import com.utegear.entity.NotificationType;
import com.utegear.entity.Order;
import com.utegear.entity.OrderStatus;
import com.utegear.entity.PaymentStatus;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.OrderRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerOrderService {

    private final OrderRepository orders;
    private final NotificationService notifications;
    private final OrderRealtimePublisher realtimePublisher;
    private final InventoryService inventoryService;

    public CustomerOrderService(OrderRepository orders, NotificationService notifications,
            OrderRealtimePublisher realtimePublisher, InventoryService inventoryService) {
        this.orders = orders;
        this.notifications = notifications;
        this.realtimePublisher = realtimePublisher;
        this.inventoryService = inventoryService;
    }

    @Transactional(readOnly = true)
    public CustomerOrderDtos.OrderPage list(User user, OrderStatus status, int page, int size) {
        requireUser(user);
        if (page < 0 || size < 1 || size > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                    "Page phải từ 0 và size phải từ 1 đến 100");
        }
        Page<Order> result = orders.findAll(ownedBy(user.getId(), status),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return CustomerOrderDtos.OrderPage.from(result);
    }

    @Transactional(readOnly = true)
    public AdminOrderDtos.Detail detail(User user, Long orderId) {
        requireUser(user);
        return AdminOrderDtos.Detail.from(orders.findDetailedByIdAndUserId(orderId, user.getId())
                .orElseThrow(this::notFound));
    }

    @Transactional
    public AdminOrderDtos.Detail cancel(User user, Long orderId, String reason) {
        requireUser(user);
        Order order = orders.findLockedByIdAndUserId(orderId, user.getId()).orElseThrow(this::notFound);
        OrderStatus previous = order.getStatus();
        if (!previous.canTransitionTo(OrderStatus.CANCELLED)) {
            throw new ApiException(HttpStatus.CONFLICT, "ORDER_CANNOT_CANCEL",
                    "Đơn hàng ở trạng thái " + previous + " không thể hủy");
        }
        if (order.getPayment() != null && order.getPayment().getStatus() == PaymentStatus.PAID) {
            throw new ApiException(HttpStatus.CONFLICT, "PAID_ORDER_CANNOT_CANCEL",
                    "Đơn đã thanh toán cần hoàn tiền trước khi hủy");
        }
        if (order.getPayment() != null && order.getPayment().getStatus() == PaymentStatus.PENDING) {
            order.getPayment().cancel();
        }
        inventoryService.restoreReservedStock(order);
        order.transitionTo(OrderStatus.CANCELLED);
        orders.save(order);
        Notification notification = notifications.create(user, NotificationType.ORDER_STATUS_CHANGED,
                "Đơn hàng đã hủy", "Đơn " + order.getOrderCode() + " đã được hủy.", order.getId().toString());
        realtimePublisher.publishStatusChangedAfterCommit(order, previous, notification);
        return AdminOrderDtos.Detail.from(order);
    }

    private Specification<Order> ownedBy(Long userId, OrderStatus status) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("user").get("id"), userId));
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void requireUser(User user) {
        if (user == null || user.getId() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Cần đăng nhập để tiếp tục");
        }
    }

    private ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng");
    }
}
