package com.utegear.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_id", unique = true, length = 100)
    private String transactionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    protected Payment() {
    }

    Payment(Order order, PaymentMethod method, BigDecimal amount) {
        if (order == null || method == null || amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Thông tin thanh toán không hợp lệ");
        }
        this.order = order;
        this.method = method;
        this.amount = amount;
    }

    public void markPaid(String transactionId) {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Chỉ thanh toán đang chờ mới có thể hoàn tất");
        }
        this.status = PaymentStatus.PAID;
        this.transactionId = transactionId == null || transactionId.isBlank() ? null : transactionId.trim();
        this.paidAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void markFailed() {
        requirePending();
        status = PaymentStatus.FAILED;
    }

    public void cancel() {
        requirePending();
        status = PaymentStatus.CANCELLED;
    }

    private void requirePending() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Trạng thái thanh toán không thể thay đổi");
        }
    }

    @PrePersist
    void create() {
        createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public PaymentMethod getMethod() { return method; }
    public PaymentStatus getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public String getTransactionId() { return transactionId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getPaidAt() { return paidAt; }
}
