package com.utegear.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, columnDefinition = "text")
    private String comment;

    @Column(name = "verified_purchase", nullable = false)
    private boolean verifiedPurchase = true;

    @Column(name = "helpful_count", nullable = false)
    private int helpfulCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();

    protected Review() {
    }

    public Review(Product product, User user, int rating, String comment, boolean verifiedPurchase) {
        if (product == null) throw new IllegalArgumentException("Sản phẩm không được để trống");
        if (user == null) throw new IllegalArgumentException("Người dùng không được để trống");
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Đánh giá phải từ 1 đến 5 sao");
        if (comment == null || comment.trim().isEmpty()) throw new IllegalArgumentException("Nội dung đánh giá không được để trống");
        this.product = product;
        this.user = user;
        this.rating = rating;
        this.comment = comment.trim();
        this.verifiedPurchase = verifiedPurchase;
    }

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void addImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            ReviewImage image = new ReviewImage(this, imageUrl.trim());
            this.images.add(image);
        }
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public User getUser() { return user; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public boolean isVerifiedPurchase() { return verifiedPurchase; }
    public int getHelpfulCount() { return helpfulCount; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public List<ReviewImage> getImages() { return images; }
}

