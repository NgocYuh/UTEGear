package com.utegear.dto.order;

import com.utegear.entity.Order;
import com.utegear.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Page;

public final class AdminOrderDtos {

    private AdminOrderDtos() {
    }

    public record StatusRequest(@NotNull OrderStatus status) {
    }

    public record Summary(Long id, String orderCode, OrderStatus status, String customerName,
                          String customerEmail, String customerPhone, Long storeId, String storeName,
                          BigDecimal total, OffsetDateTime createdAt) {
        public static Summary from(Order order) {
            return new Summary(order.getId(), order.getOrderCode(), order.getStatus(), order.getCustomerName(),
                    order.getCustomerEmail(), order.getCustomerPhone(),
                    order.getFulfillmentStore() == null ? null : order.getFulfillmentStore().getId(),
                    order.getFulfillmentStore() == null ? null : order.getFulfillmentStore().getName(),
                    order.getTotal(), order.getCreatedAt());
        }
    }

    public record OrderPage(List<Summary> content, int page, int size, long totalElements, int totalPages) {
        public static OrderPage from(Page<Order> source) {
            return new OrderPage(source.getContent().stream().map(Summary::from).toList(),
                    source.getNumber(), source.getSize(), source.getTotalElements(), source.getTotalPages());
        }

        public int number() { return page; }
        public boolean isFirst() { return page == 0; }
        public boolean isLast() { return totalPages == 0 || page >= totalPages - 1; }
        public boolean isEmpty() { return content.isEmpty(); }
    }

    public record Detail(Long id, String orderCode, OrderStatus status, Customer customer, Shipping shipping,
                         StoreRef fulfillmentStore, String couponCode, BigDecimal subtotal, BigDecimal discount,
                         BigDecimal shippingFee, BigDecimal total, String note, PaymentRef payment,
                         List<Item> items, OffsetDateTime createdAt, OffsetDateTime updatedAt,
                         OffsetDateTime confirmedAt, OffsetDateTime processingAt, OffsetDateTime shippedAt,
                         OffsetDateTime completedAt, OffsetDateTime cancelledAt) {
        public static Detail from(Order order) {
            return new Detail(order.getId(), order.getOrderCode(), order.getStatus(),
                    new Customer(order.getCustomerName(), order.getCustomerEmail(), order.getCustomerPhone()),
                    new Shipping(order.getShippingStreet(), order.getShippingWard(), order.getShippingDistrict(),
                            order.getShippingProvince(), order.getShippingLabel(), order.getShippingMethod().name()),
                    order.getFulfillmentStore() == null ? null
                            : new StoreRef(order.getFulfillmentStore().getId(), order.getFulfillmentStore().getName()),
                    order.getCouponCode(), order.getSubtotal(), order.getDiscount(), order.getShippingFee(),
                    order.getTotal(), order.getNote(),
                    order.getPayment() == null ? null : new PaymentRef(order.getPayment().getMethod().name(),
                            order.getPayment().getStatus().name(), order.getPayment().getAmount()),
                    order.getItems().stream().map(item -> new Item(item.getVariant().getId(), item.getSku(),
                            item.getProductName(), item.getOptionValues(), item.getImageUrl(), item.getUnitPrice(),
                            item.getQuantity(), item.getSubtotal())).toList(),
                    order.getCreatedAt(), order.getUpdatedAt(), order.getConfirmedAt(), order.getProcessingAt(),
                    order.getShippedAt(), order.getCompletedAt(), order.getCancelledAt());
        }
    }

    public record Customer(String name, String email, String phone) { }
    public record Shipping(String street, String ward, String district, String province, String label, String method) { }
    public record StoreRef(Long id, String name) { }
    public record PaymentRef(String method, String status, BigDecimal amount) { }
    public record Item(Long variantId, String sku, String productName, java.util.Map<String, String> optionValues,
                       String imageUrl, BigDecimal unitPrice, int quantity, BigDecimal subtotal) { }
}
