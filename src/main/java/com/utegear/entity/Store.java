package com.utegear.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "stores")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 100)
    private String province;

    @Column(length = 100)
    private String district;

    @Column(unique = true, length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "opening_time", nullable = false)
    private LocalTime openingTime;

    @Column(name = "closing_time", nullable = false)
    private LocalTime closingTime;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Store() {
    }

    public Store(
            String name,
            String address,
            String province,
            String district,
            String phone,
            String email,
            String imageUrl,
            LocalTime openingTime,
            LocalTime closingTime,
            String description,
            BigDecimal latitude,
            BigDecimal longitude,
            boolean active) {
        update(name, address, province, district, phone, email, imageUrl, openingTime,
                closingTime, description, latitude, longitude, active);
    }

    public void update(
            String name,
            String address,
            String province,
            String district,
            String phone,
            String email,
            String imageUrl,
            LocalTime openingTime,
            LocalTime closingTime,
            String description,
            BigDecimal latitude,
            BigDecimal longitude,
            boolean active) {
        this.name = name;
        this.address = address;
        this.province = province;
        this.district = district;
        this.phone = phone;
        this.email = email;
        this.imageUrl = imageUrl;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.active = active;
        validateHours();
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @PrePersist
    void create() {
        validateHours();
        createdAt = updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    void touch() {
        validateHours();
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    private void validateHours() {
        if (openingTime == null || closingTime == null || openingTime.equals(closingTime)) {
            throw new IllegalArgumentException("Giờ mở cửa và đóng cửa phải khác nhau");
        }
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getProvince() { return province; }
    public String getDistrict() { return district; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getImageUrl() { return imageUrl; }
    public LocalTime getOpeningTime() { return openingTime; }
    public LocalTime getClosingTime() { return closingTime; }
    public String getDescription() { return description; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public boolean isActive() { return active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
