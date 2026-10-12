package com.utegear.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "secure_url", nullable = false, length = 500)
    private String secureUrl;

    @Column(name = "public_id", nullable = false, unique = true, length = 255)
    private String publicId;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    protected ProductImage() {
    }

    public ProductImage(
            String secureUrl,
            String publicId,
            String altText,
            int sortOrder,
            boolean primary) {
        updateMedia(secureUrl, publicId, altText, primary);
        setSortOrder(sortOrder);
    }

    void attach(Product product) {
        this.product = product;
    }

    public void updateMedia(String secureUrl, String publicId, String altText, boolean primary) {
        if (secureUrl == null || secureUrl.isBlank() || publicId == null || publicId.isBlank()) {
            throw new IllegalArgumentException("Thông tin Cloudinary không hợp lệ");
        }
        this.secureUrl = secureUrl;
        this.publicId = publicId;
        this.altText = normalize(altText);
        this.primary = primary;
    }

    public void updateMetadata(String altText, boolean primary) {
        this.altText = normalize(altText);
        this.primary = primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public void setSortOrder(int sortOrder) {
        if (sortOrder < 0) {
            throw new IllegalArgumentException("Thứ tự ảnh không được âm");
        }
        this.sortOrder = sortOrder;
    }

    public void setTemporarySortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public String getSecureUrl() { return secureUrl; }
    public String getPublicId() { return publicId; }
    public String getAltText() { return altText; }
    public int getSortOrder() { return sortOrder; }
    public boolean isPrimary() { return primary; }
}
