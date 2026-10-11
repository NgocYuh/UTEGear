package com.utegear.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity @Table(name = "categories")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=100) private String name;
    @Column(nullable=false, unique=true, length=120) private String slug;
    @Column(length=500) private String description;
    @Column(name="image_url", length=500) private String imageUrl;
    @Column(nullable=false) private boolean active = true;
    @Column(name="sort_order", nullable=false) private int sortOrder;
    @Column(name="created_at", nullable=false, updatable=false) private OffsetDateTime createdAt;
    @Column(name="updated_at", nullable=false) private OffsetDateTime updatedAt;
    protected Category() {}
    public Category(String name, String slug, String description, int sortOrder) { this.name=name; this.slug=slug; this.description=description; this.sortOrder=sortOrder; }
    @PrePersist void create(){ createdAt=updatedAt=OffsetDateTime.now(ZoneOffset.UTC); }
    @PreUpdate void update(){ updatedAt=OffsetDateTime.now(ZoneOffset.UTC); }
    public Long getId(){return id;} public String getName(){return name;} public String getSlug(){return slug;} public String getDescription(){return description;} public String getImageUrl(){return imageUrl;} public boolean isActive(){return active;} public int getSortOrder(){return sortOrder;}
}
