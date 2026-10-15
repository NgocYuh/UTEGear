package com.utegear.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity @Table(name = "brands")
public class Brand {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=100) private String name;
    @Column(nullable=false, unique=true, length=120) private String slug;
    @Column(name="logo_url", length=500) private String logoUrl;
    @Column(length=500) private String description;
    @Column(nullable=false) private boolean active = true;
    @Column(name="created_at", nullable=false, updatable=false) private OffsetDateTime createdAt;
    @Column(name="updated_at", nullable=false) private OffsetDateTime updatedAt;
    protected Brand() {}
    public Brand(String name, String slug, String description){this.name=name;this.slug=slug;this.description=description;}
    public void update(String name, String slug, String description, boolean active){this.name=name;this.slug=slug;this.description=description;this.active=active;}
    @PrePersist void create(){createdAt=updatedAt=OffsetDateTime.now(ZoneOffset.UTC);}
    @PreUpdate void update(){updatedAt=OffsetDateTime.now(ZoneOffset.UTC);}
    public Long getId(){return id;} public String getName(){return name;} public String getSlug(){return slug;} public String getLogoUrl(){return logoUrl;} public String getDescription(){return description;} public boolean isActive(){return active;}
}
