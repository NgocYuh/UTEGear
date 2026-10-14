package com.utegear.dto.order;

import com.utegear.entity.Order;
import com.utegear.entity.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Page;

public final class CustomerOrderDtos {

    private CustomerOrderDtos() {
    }

    public record CancelRequest(
            @NotBlank(message = "Lý do hủy đơn là bắt buộc")
            @Size(max = 500, message = "Lý do hủy đơn không được vượt quá 500 ký tự")
            String reason) {
    }

    public record Summary(Long id, String orderCode, OrderStatus status, String paymentStatus,
                          int itemCount, BigDecimal total, OffsetDateTime createdAt) {
        public static Summary from(Order order) {
            return new Summary(order.getId(), order.getOrderCode(), order.getStatus(),
                    order.getPayment() == null ? null : order.getPayment().getStatus().name(),
                    order.getItems().size(), order.getTotal(), order.getCreatedAt());
        }
    }

    public record OrderPage(List<Summary> content, int page, int size, long totalElements,
                            int totalPages, boolean first, boolean last, boolean empty) {
        public static OrderPage from(Page<Order> source) {
            return new OrderPage(source.getContent().stream().map(Summary::from).toList(),
                    source.getNumber(), source.getSize(), source.getTotalElements(), source.getTotalPages(),
                    source.isFirst(), source.isLast(), source.isEmpty());
        }

        public int number() {
            return page;
        }

        public boolean isEmpty() {
            return empty;
        }
    }
}
