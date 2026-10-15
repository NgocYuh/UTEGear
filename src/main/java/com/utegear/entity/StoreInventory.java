package com.utegear.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(
        name = "store_inventory",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_store_inventory_store_variant",
                columnNames = {"store_id", "variant_id"}))
public class StoreInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected StoreInventory() {
    }

    public StoreInventory(Store store, ProductVariant variant) {
        this.store = store;
        this.variant = variant;
    }

    public void receive(int amount) {
        requirePositive(amount);
        quantity = Math.addExact(quantity, amount);
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void decrease(int amount) {
        requirePositive(amount);
        if (quantity < amount) {
            throw new IllegalStateException("Không đủ tồn kho");
        }
        quantity -= amount;
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Tồn kho không được âm");
        }
        this.quantity = quantity;
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @PrePersist
    @PreUpdate
    void touch() {
        if (quantity < 0) {
            throw new IllegalStateException("Tồn kho không được âm");
        }
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    private void requirePositive(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
    }

    public Long getId() { return id; }
    public Store getStore() { return store; }
    public ProductVariant getVariant() { return variant; }
    public int getQuantity() { return quantity; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
