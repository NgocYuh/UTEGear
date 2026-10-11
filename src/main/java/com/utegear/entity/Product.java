package com.utegear.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity @Table(name="products")
public class Product {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="category_id") private Category category;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="brand_id") private Brand brand;
 @Column(nullable=false,length=200) private String name;
 @Column(nullable=false,unique=true,length=220) private String slug;
 @Column(columnDefinition="TEXT") private String description;
 @JdbcTypeCode(SqlTypes.JSON) @Column(nullable=false,columnDefinition="jsonb") private JsonNode specifications=JsonNodeFactory.instance.objectNode();
 @Column(name="base_price",nullable=false,precision=12,scale=2) private BigDecimal basePrice;
 @Column(name="sale_price",precision=12,scale=2) private BigDecimal salePrice;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private ProductStatus status=ProductStatus.ACTIVE;
 @Column(name="rating_average",nullable=false,precision=3,scale=2) private BigDecimal ratingAverage=BigDecimal.ZERO;
 @Column(name="review_count",nullable=false) private int reviewCount;
 @Column(name="sold_count",nullable=false) private int soldCount;
 @Column(name="created_at",nullable=false,updatable=false) private OffsetDateTime createdAt;
 @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
 @OneToMany(mappedBy="product",cascade=CascadeType.ALL,orphanRemoval=true) private List<ProductVariant> variants=new ArrayList<>();
 @OneToMany(mappedBy="product",cascade=CascadeType.ALL,orphanRemoval=true) private List<ProductImage> images=new ArrayList<>();
 protected Product(){}
 public Product(String name,String slug,Category category,Brand brand,BigDecimal basePrice,BigDecimal salePrice){this.name=name;this.slug=slug;this.category=category;this.brand=brand;this.basePrice=basePrice;this.salePrice=salePrice;validatePrice();}
 public void addVariant(ProductVariant variant){variant.attach(this);variants.add(variant);}
 public void addImage(ProductImage image){image.attach(this);images.add(image);}
 @PrePersist void create(){validate();createdAt=updatedAt=OffsetDateTime.now(ZoneOffset.UTC);}
 @PreUpdate void update(){validate();updatedAt=OffsetDateTime.now(ZoneOffset.UTC);}
 private void validate(){validatePrice();if(variants.isEmpty())throw new IllegalStateException("Sản phẩm phải có ít nhất một biến thể");if(variants.stream().filter(ProductVariant::isDefault).count()!=1)throw new IllegalStateException("Sản phẩm phải có đúng một biến thể mặc định");}
 private void validatePrice(){if(basePrice==null||basePrice.signum()<0)throw new IllegalArgumentException("Giá cơ sở không hợp lệ");if(salePrice!=null&&(salePrice.signum()<0||salePrice.compareTo(basePrice)>0))throw new IllegalArgumentException("Giá khuyến mãi không hợp lệ");}
 public Long getId(){return id;} public Category getCategory(){return category;} public Brand getBrand(){return brand;} public String getName(){return name;} public String getSlug(){return slug;} public String getDescription(){return description;} public JsonNode getSpecifications(){return specifications;} public BigDecimal getBasePrice(){return basePrice;} public BigDecimal getSalePrice(){return salePrice;} public BigDecimal getEffectivePrice(){return salePrice==null?basePrice:salePrice;} public ProductStatus getStatus(){return status;} public BigDecimal getRatingAverage(){return ratingAverage;} public int getReviewCount(){return reviewCount;} public int getSoldCount(){return soldCount;} public OffsetDateTime getCreatedAt(){return createdAt;} public OffsetDateTime getUpdatedAt(){return updatedAt;} public List<ProductVariant> getVariants(){return variants;} public List<ProductImage> getImages(){return images;}
}
