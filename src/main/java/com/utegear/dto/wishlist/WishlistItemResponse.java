package com.utegear.dto.wishlist;

import com.utegear.entity.Product;
import com.utegear.entity.ProductImage;
import com.utegear.entity.ProductVariant;
import com.utegear.entity.WishlistItem;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

public record WishlistItemResponse(ProductSummary product, OffsetDateTime createdAt) {

    public static WishlistItemResponse from(WishlistItem item, Map<Long, Long> stockByVariant) {
        return new WishlistItemResponse(ProductSummary.from(item.getProduct(), stockByVariant), item.getCreatedAt());
    }

    public record ProductSummary(
            Long id,
            String name,
            String slug,
            BigDecimal effectivePrice,
            Image primaryImage,
            Long defaultVariantId,
            boolean inStock) {

        static ProductSummary from(Product product, Map<Long, Long> stockByVariant) {
            Image primaryImage = product.getImages().stream()
                    .filter(ProductImage::isPrimary)
                    .findFirst()
                    .map(Image::from)
                    .orElse(null);
            Long defaultVariantId = product.getVariants().stream()
                    .filter(ProductVariant::isDefault)
                    .map(ProductVariant::getId)
                    .findFirst()
                    .orElse(null);
            boolean inStock = product.getVariants().stream()
                    .filter(ProductVariant::isActive)
                    .map(ProductVariant::getId)
                    .anyMatch(id -> stockByVariant.getOrDefault(id, 0L) > 0);
            return new ProductSummary(product.getId(), product.getName(), product.getSlug(),
                    product.getEffectivePrice(), primaryImage, defaultVariantId, inStock);
        }
    }

    public record Image(Long id, String secureUrl, String altText) {
        static Image from(ProductImage image) {
            return new Image(image.getId(), image.getSecureUrl(), image.getAltText());
        }
    }
}
