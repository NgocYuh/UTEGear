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
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "review_images")
public class ReviewImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected ReviewImage() {
    }

    public ReviewImage(Review review, String imageUrl) {
        if (review == null) throw new IllegalArgumentException("Đánh giá không được để trống");
        if (imageUrl == null || imageUrl.trim().isEmpty()) throw new IllegalArgumentException("Đường dẫn ảnh không được để trống");
        this.review = review;
        this.imageUrl = imageUrl.trim();
    }

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() { return id; }
    public Review getReview() { return review; }
    public String getImageUrl() { return imageUrl; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}

