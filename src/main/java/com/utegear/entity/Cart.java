package com.utegear.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
import java.util.Set;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    protected Cart() {
    }

    public Cart(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Người dùng của giỏ hàng không được để trống");
        }
        this.user = user;
    }

    public CartItem add(ProductVariant variant, int quantity) {
        CartItem existing = findItem(variant);
        if (existing != null) {
            existing.increase(quantity);
            touch();
            return existing;
        }
        CartItem item = new CartItem(this, variant, quantity);
        items.add(item);
        touch();
        return item;
    }

    public void remove(CartItem item) {
        if (items.remove(item)) {
            touch();
        }
    }

    public void changeQuantity(CartItem item, int quantity) {
        if (item == null || item.getCart() != this || !items.contains(item)) {
            throw new IllegalArgumentException("Dòng sản phẩm không thuộc giỏ hàng");
        }
        item.changeQuantity(quantity);
        touch();
    }

    public void clear() {
        if (!items.isEmpty()) {
            items.clear();
            touch();
        }
    }

    public void removeItemsByVariantIds(Set<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return;
        }
        boolean removed = items.removeIf(item -> item.getVariant() != null && variantIds.contains(item.getVariant().getId()));
        if (removed) {
            touch();
        }
    }

    public int getItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal getSubtotal() {
        return items.stream().map(CartItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CartItem findItem(ProductVariant variant) {
        if (variant == null || variant.getId() == null) {
            return null;
        }
        return items.stream()
                .filter(item -> variant.getId().equals(item.getVariant().getId()))
                .findFirst()
                .orElse(null);
    }

    @PrePersist
    void create() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void update() {
        touch();
    }

    private void touch() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
