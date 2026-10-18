package com.utegear.dto.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.utegear.entity.Brand;
import com.utegear.entity.Category;
import com.utegear.entity.Coupon;
import com.utegear.entity.DiscountType;
import com.utegear.entity.Product;
import com.utegear.entity.ProductImage;
import com.utegear.entity.ProductStatus;
import com.utegear.entity.ProductVariant;
import com.utegear.entity.StoreInventory;
import com.utegear.entity.User;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AdminDtos {
    private AdminDtos() {
    }

    public record CategoryRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 500) String description,
            @Min(0) int sortOrder,
            boolean active) {
    }

    public record CategoryView(
            Long id, String name, String slug, String description, int sortOrder, boolean active) {
        public static CategoryView from(Category category) {
            return new CategoryView(category.getId(), category.getName(), category.getSlug(),
                    category.getDescription(), category.getSortOrder(), category.isActive());
        }
    }

    public record BrandRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 500) String description,
            boolean active) {
    }

    public record BrandView(
            Long id, String name, String slug, String description, String logoUrl, boolean active) {
        public static BrandView from(Brand brand) {
            return new BrandView(brand.getId(), brand.getName(), brand.getSlug(),
                    brand.getDescription(), brand.getLogoUrl(), brand.isActive());
        }
    }

    public record ProductRequest(
            @NotBlank @Size(max = 200) String name,
            @NotNull @Positive Long categoryId,
            @Positive Long brandId,
            @Size(max = 10000) String description,
            @NotNull @DecimalMin("1000.00") @DecimalMax("999999999.99") BigDecimal basePrice,
            @DecimalMin("0.00") @DecimalMax("999999999.99") BigDecimal salePrice,
            @NotNull ProductStatus status,
            @NotBlank @Size(max = 50) String defaultSku,
            Map<String, String> optionValues,
            JsonNode specifications) {
    }

    public record ProductView(
            Long id,
            String name,
            String slug,
            String description,
            Long categoryId,
            String categoryName,
            Long brandId,
            String brandName,
            BigDecimal basePrice,
            BigDecimal salePrice,
            ProductStatus status,
            Long defaultVariantId,
            String defaultSku,
            Map<String, String> optionValues,
            List<ProductVariantView> variants,
            List<ProductImageView> images,
            JsonNode specifications,
            int variantCount,
            long totalStock) {
        public static ProductView from(Product product, long totalStock) {
            ProductVariant defaultVariant = product.getVariants().stream()
                    .filter(ProductVariant::isDefault)
                    .findFirst()
                    .orElse(null);
            Brand brand = product.getBrand();
            return new ProductView(
                    product.getId(), product.getName(), product.getSlug(), product.getDescription(),
                    product.getCategory().getId(), product.getCategory().getName(),
                    brand == null ? null : brand.getId(), brand == null ? null : brand.getName(),
                    product.getBasePrice(), product.getSalePrice(), product.getStatus(),
                    defaultVariant == null ? null : defaultVariant.getId(),
                    defaultVariant == null ? null : defaultVariant.getSku(),
                    defaultVariant == null ? Map.of() : defaultVariant.getOptionValues(),
                    product.getVariants().stream().map(ProductVariantView::from).toList(),
                    product.getImages().stream().map(ProductImageView::from).toList(),
                    product.getSpecifications(), product.getVariants().size(), totalStock);
        }
    }

    public record ProductVariantView(
            Long id, String sku, Map<String, String> optionValues,
            BigDecimal priceAdjustment, boolean defaultVariant, boolean active) {
        public static ProductVariantView from(ProductVariant variant) {
            return new ProductVariantView(
                    variant.getId(), variant.getSku(), variant.getOptionValues(),
                    variant.getPriceAdjustment(), variant.isDefault(), variant.isActive());
        }
    }

    public record ProductImageView(
            Long id, String secureUrl, String altText, boolean primary, int sortOrder) {
        public static ProductImageView from(ProductImage image) {
            return new ProductImageView(
                    image.getId(), image.getSecureUrl(), image.getAltText(),
                    image.isPrimary(), image.getSortOrder());
        }
    }

    public record ProductPage(
            List<ProductView> content, int page, int size, long totalElements, int totalPages,
            boolean first, boolean last, boolean empty) {
    }

    public record ProductStatusRequest(@NotNull ProductStatus status) {
    }

    public record CouponRequest(
            @NotBlank @Size(max = 50) String code,
            @NotNull DiscountType discountType,
            @NotNull @DecimalMin("0.01") @DecimalMax("999999999.99") BigDecimal discountValue,
            @DecimalMin("0.00") @DecimalMax("999999999.99") BigDecimal minimumOrderValue,
            @DecimalMin("0.00") @DecimalMax("999999999.99") BigDecimal maximumDiscount,
            @NotNull OffsetDateTime validFrom,
            @NotNull OffsetDateTime validUntil,
            @PositiveOrZero @Max(1000000) int maxUses,
            boolean active) {
    }

    public record CouponView(
            Long id,
            String code,
            DiscountType discountType,
            BigDecimal discountValue,
            BigDecimal minimumOrderValue,
            BigDecimal maximumDiscount,
            OffsetDateTime validFrom,
            OffsetDateTime validUntil,
            int maxUses,
            int usedCount,
            boolean active) {
        public static CouponView from(Coupon coupon) {
            return new CouponView(coupon.getId(), coupon.getCode(), coupon.getDiscountType(),
                    coupon.getDiscountValue(), coupon.getMinimumOrderValue(), coupon.getMaximumDiscount(),
                    coupon.getValidFrom(), coupon.getValidUntil(), coupon.getMaxUses(),
                    coupon.getUsedCount(), coupon.isActive());
        }
    }

    public record UserView(
            Long id,
            String fullName,
            String email,
            String phone,
            boolean enabled,
            Set<String> roles,
            Long assignedStoreId,
            String assignedStoreName,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
        public static UserView from(User user) {
            return new UserView(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(),
                    user.isEnabled(), user.getRoles().stream().map(role -> role.getName()).collect(java.util.stream.Collectors.toUnmodifiableSet()),
                    user.getAssignedStore() == null ? null : user.getAssignedStore().getId(),
                    user.getAssignedStore() == null ? null : user.getAssignedStore().getName(),
                    user.getCreatedAt(), user.getUpdatedAt());
        }
    }

    public record UserEnabledRequest(@NotNull Boolean enabled) {
    }

    public record UserRolesRequest(@NotEmpty Set<String> roles) {
    }

    public record UserStoreAssignmentRequest(@Positive Long storeId) {
    }

    public record InventoryQuantityRequest(@NotNull @PositiveOrZero @Max(1000000) Integer quantity) {
    }

    public record InventoryTransferRequest(
            @NotNull @Positive Long sourceStoreId,
            @NotNull @Positive Long destinationStoreId,
            @NotNull @Positive Long variantId,
            @Min(1) @Max(1000000) int quantity) {
    }

    public record InventoryView(
            Long storeId,
            String storeName,
            Long variantId,
            String sku,
            Long productId,
            String productName,
            int quantity,
            OffsetDateTime updatedAt) {
        public static InventoryView from(StoreInventory inventory) {
            return new InventoryView(
                    inventory.getStore().getId(), inventory.getStore().getName(),
                    inventory.getVariant().getId(), inventory.getVariant().getSku(),
                    inventory.getVariant().getProduct().getId(), inventory.getVariant().getProduct().getName(),
                    inventory.getQuantity(), inventory.getUpdatedAt());
        }
    }

    public record DashboardSummary(
            long totalOrders,
            long pendingOrders,
            long totalProducts,
            long activeProducts,
            long totalStores,
            long totalUsers,
            long totalCategories,
            long totalBrands,
            long totalCoupons) {
    }
}
