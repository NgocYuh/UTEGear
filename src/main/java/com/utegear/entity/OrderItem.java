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
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(nullable = false, length = 50)
    private String sku;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "option_values", nullable = false, columnDefinition = "jsonb")
    private Map<String, String> optionValues = new LinkedHashMap<>();

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    protected OrderItem() {
    }

    OrderItem(Order order, ProductVariant variant, int quantity, String imageUrl) {
        if (order == null || variant == null || variant.getProduct() == null) {
            throw new IllegalArgumentException("Đơn hàng và biến thể hợp lệ là bắt buộc");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        this.order = order;
        this.variant = variant;
        this.productName = variant.getProduct().getName();
        this.sku = variant.getSku();
        this.optionValues = new LinkedHashMap<>(variant.getOptionValues());
        this.imageUrl = normalize(imageUrl);
        this.unitPrice = variant.getProduct().getEffectivePrice().add(variant.getPriceAdjustment());
        this.quantity = quantity;
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public ProductVariant getVariant() { return variant; }
    public String getProductName() { return productName; }
    public String getSku() { return sku; }
    public Map<String, String> getOptionValues() { return Map.copyOf(optionValues); }
    public String getImageUrl() { return imageUrl; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public BigDecimal getSubtotal() { return subtotal; }
}
