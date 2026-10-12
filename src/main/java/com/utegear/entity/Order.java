package com.utegear.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", nullable = false, unique = true, length = 50)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fulfillment_store_id")
    private Store fulfillmentStore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 20)
    private String customerPhone;

    @Column(name = "customer_email", nullable = false, length = 100)
    private String customerEmail;

    @Column(name = "shipping_street", nullable = false, length = 255)
    private String shippingStreet;

    @Column(name = "shipping_ward", length = 100)
    private String shippingWard;

    @Column(name = "shipping_district", length = 100)
    private String shippingDistrict;

    @Column(name = "shipping_province", nullable = false, length = 100)
    private String shippingProvince;

    @Column(name = "shipping_label", length = 100)
    private String shippingLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_method", nullable = false, length = 30)
    private ShippingMethod shippingMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(length = 500)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "confirmed_at")
    private OffsetDateTime confirmedAt;

    @Column(name = "processing_at")
    private OffsetDateTime processingAt;

    @Column(name = "shipped_at")
    private OffsetDateTime shippedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    protected Order() {
    }

    public Order(
            String orderCode,
            User user,
            Store fulfillmentStore,
            Coupon coupon,
            Address shippingAddress,
            ShippingMethod shippingMethod,
            String note) {
        if (user == null
                || user.getId() == null
                || shippingAddress == null
                || !Objects.equals(shippingAddress.getUser().getId(), user.getId())) {
            throw new IllegalArgumentException("Người dùng và địa chỉ giao hàng không hợp lệ");
        }
        this.orderCode = requireText(orderCode, "Mã đơn hàng");
        this.user = user;
        this.fulfillmentStore = fulfillmentStore;
        this.coupon = coupon;
        this.couponCode = coupon == null ? null : coupon.getCode();
        this.customerName = shippingAddress.getFullName();
        this.customerPhone = shippingAddress.getPhone();
        this.customerEmail = requireText(user.getEmail(), "Email khách hàng");
        this.shippingStreet = shippingAddress.getStreet();
        this.shippingWard = shippingAddress.getWard();
        this.shippingDistrict = shippingAddress.getDistrict();
        this.shippingProvince = shippingAddress.getProvince();
        this.shippingLabel = shippingAddress.getLabel();
        this.shippingMethod = shippingMethod == null ? ShippingMethod.HOME_DELIVERY : shippingMethod;
        this.note = normalize(note);
    }

    public OrderItem addItem(ProductVariant variant, int quantity, String imageUrl) {
        if (items.stream().anyMatch(item -> item.getVariant().equals(variant))) {
            throw new IllegalArgumentException("Mỗi biến thể chỉ xuất hiện một lần trong đơn hàng");
        }
        OrderItem item = new OrderItem(this, variant, quantity, imageUrl);
        items.add(item);
        recalculate();
        return item;
    }

    public void applyPricing(BigDecimal discount, BigDecimal shippingFee) {
        if (discount == null || shippingFee == null || discount.signum() < 0 || shippingFee.signum() < 0) {
            throw new IllegalArgumentException("Giảm giá và phí vận chuyển không được âm");
        }
        if (discount.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException("Giảm giá không được vượt quá subtotal");
        }
        this.discount = discount;
        this.shippingFee = shippingFee;
        recalculate();
    }

    public Payment createPayment(PaymentMethod method) {
        if (payment != null) {
            throw new IllegalStateException("Đơn hàng đã có thanh toán");
        }
        payment = new Payment(this, method, total);
        return payment;
    }

    private void recalculate() {
        subtotal = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        total = subtotal.subtract(discount).add(shippingFee);
    }

    @PrePersist
    void create() {
        validate();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void update() {
        validate();
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    private void validate() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Đơn hàng phải có ít nhất một sản phẩm");
        }
        if (total.signum() < 0 || total.compareTo(subtotal.subtract(discount).add(shippingFee)) != 0) {
            throw new IllegalStateException("Tổng tiền đơn hàng không hợp lệ");
        }
    }

    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " không được để trống");
        }
        return value.trim();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Long getId() { return id; }
    public String getOrderCode() { return orderCode; }
    public User getUser() { return user; }
    public Store getFulfillmentStore() { return fulfillmentStore; }
    public Coupon getCoupon() { return coupon; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getCustomerEmail() { return customerEmail; }
    public String getShippingStreet() { return shippingStreet; }
    public String getShippingWard() { return shippingWard; }
    public String getShippingDistrict() { return shippingDistrict; }
    public String getShippingProvince() { return shippingProvince; }
    public String getShippingLabel() { return shippingLabel; }
    public ShippingMethod getShippingMethod() { return shippingMethod; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public BigDecimal getTotal() { return total; }
    public String getCouponCode() { return couponCode; }
    public String getNote() { return note; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public OffsetDateTime getConfirmedAt() { return confirmedAt; }
    public OffsetDateTime getProcessingAt() { return processingAt; }
    public OffsetDateTime getShippedAt() { return shippedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public OffsetDateTime getCancelledAt() { return cancelledAt; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public Payment getPayment() { return payment; }
}
