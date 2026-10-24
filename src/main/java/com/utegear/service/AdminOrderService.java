package com.utegear.service;

import com.utegear.dto.order.AdminOrderDtos;
import com.utegear.entity.Order;
import com.utegear.entity.OrderStatus;
import com.utegear.entity.Notification;
import com.utegear.entity.NotificationType;
import com.utegear.entity.PaymentMethod;
import com.utegear.entity.PaymentStatus;
import com.utegear.exception.ApiException;
import com.utegear.repository.OrderRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminOrderService {

    private final OrderRepository orders;
    private final NotificationService notificationService;
    private final OrderRealtimePublisher realtimePublisher;
    private final InventoryService inventoryService;

    public AdminOrderService(OrderRepository orders, NotificationService notificationService,
            OrderRealtimePublisher realtimePublisher, InventoryService inventoryService) {
        this.orders = orders;
        this.notificationService = notificationService;
        this.realtimePublisher = realtimePublisher;
        this.inventoryService = inventoryService;
    }

    @Transactional(readOnly = true)
    public AdminOrderDtos.OrderPage list(String query, OrderStatus status, Long storeId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw validation("Page phải từ 0 và size phải từ 1 đến 100");
        }
        Page<Order> result = orders.findAll(specification(query, status, storeId),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return AdminOrderDtos.OrderPage.from(result);
    }

    @Transactional(readOnly = true)
    public AdminOrderDtos.Detail detail(Long orderId) {
        return AdminOrderDtos.Detail.from(orders.findDetailedById(orderId).orElseThrow(this::notFound));
    }

    @Transactional
    public AdminOrderDtos.Detail updateStatus(Long orderId, OrderStatus nextStatus) {
        Order order = orders.findLockedById(orderId).orElseThrow(this::notFound);
        OrderStatus previousStatus = order.getStatus();
        if (!order.getStatus().canTransitionTo(nextStatus)) {
            throw new ApiException(HttpStatus.CONFLICT, "INVALID_ORDER_TRANSITION",
                    "Không thể chuyển trạng thái đơn hàng từ " + order.getStatus() + " sang " + nextStatus);
        }
        if (nextStatus == OrderStatus.CONFIRMED && order.getPayment() != null) {
            if (order.getPayment().getMethod() != PaymentMethod.COD && order.getPayment().getStatus() != PaymentStatus.PAID) {
                throw new ApiException(HttpStatus.CONFLICT, "UNPAID_ONLINE_ORDER",
                        "Không thể xác nhận đơn hàng thanh toán trực tuyến khi khách chưa thanh toán");
            }
        }
        if (nextStatus == OrderStatus.CANCELLED && order.getPayment() != null) {
            if (order.getPayment().getStatus() == PaymentStatus.PAID) {
                throw new ApiException(HttpStatus.CONFLICT, "PAID_ORDER_CANNOT_CANCEL",
                        "Đơn đã thanh toán cần hoàn tiền trước khi hủy");
            }
            if (order.getPayment().getStatus() == PaymentStatus.PENDING) {
                order.getPayment().cancel();
            }
        }
        if (nextStatus == OrderStatus.CANCELLED) {
            inventoryService.restoreReservedStock(order);
        }
        if (nextStatus == OrderStatus.COMPLETED
                && order.getPayment() != null
                && order.getPayment().getMethod() == PaymentMethod.COD
                && order.getPayment().getStatus() == PaymentStatus.PENDING) {
            order.getPayment().markPaid(null);
        }
        order.transitionTo(nextStatus);
        orders.save(order);
        Notification notification = notificationService.create(
                order.getUser(), NotificationType.ORDER_STATUS_CHANGED,
                notificationTitle(nextStatus), notificationMessage(order, nextStatus), order.getId().toString());
        realtimePublisher.publishStatusChangedAfterCommit(order, previousStatus, notification);
        return AdminOrderDtos.Detail.from(order);
    }

    private String notificationTitle(OrderStatus status) {
        return switch (status) {
            case CONFIRMED -> "Đơn hàng đã được xác nhận";
            case PROCESSING -> "Đơn hàng đang được xử lý";
            case SHIPPING -> "Đơn hàng đang được giao";
            case COMPLETED -> "Đơn hàng đã hoàn thành";
            case CANCELLED -> "Đơn hàng đã bị hủy";
            default -> throw new IllegalArgumentException("Không hỗ trợ thông báo cho trạng thái " + status);
        };
    }

    private String notificationMessage(Order order, OrderStatus status) {
        return "Đơn " + order.getOrderCode() + " đã chuyển sang trạng thái " + status.name() + ".";
    }

    private Specification<Order> specification(String query, OrderStatus status, Long storeId) {
        return (root, criteriaQuery, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("orderCode")), pattern),
                        builder.like(builder.lower(root.get("customerEmail")), pattern),
                        builder.like(builder.lower(root.get("customerPhone")), pattern)));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (storeId != null) {
                predicates.add(builder.equal(root.get("fulfillmentStore").get("id"), storeId));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private ApiException validation(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
    }

    private ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng");
    }
}
