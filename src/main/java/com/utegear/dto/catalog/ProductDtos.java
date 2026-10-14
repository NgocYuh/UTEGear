package com.utegear.dto.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.utegear.entity.Brand;
import com.utegear.entity.Product;
import com.utegear.entity.ProductImage;
import com.utegear.entity.ProductVariant;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

public final class ProductDtos {
    private ProductDtos() {
    }

    public record Image(Long id, String secureUrl, String altText, boolean primary, int sortOrder) {
        static Image from(ProductImage image) {
            return new Image(image.getId(), image.getSecureUrl(), image.getAltText(),
                    image.isPrimary(), image.getSortOrder());
        }
    }

    public record Variant(
            Long id, String sku, Map<String, String> optionValues, boolean isDefault,
            BigDecimal priceAdjustment, BigDecimal effectivePrice, long totalStock, boolean inStock) {
        static Variant from(ProductVariant variant, BigDecimal basePrice, Map<Long, Long> stockByVariant) {
            BigDecimal price = basePrice.add(variant.getPriceAdjustment());
            long totalStock = stockByVariant.getOrDefault(variant.getId(), 0L);
            return new Variant(variant.getId(), variant.getSku(), variant.getOptionValues(),
                    variant.isDefault(), variant.getPriceAdjustment(), price, totalStock, totalStock > 0);
        }
    }

    public record Summary(
            Long id, String name, String slug, CategorySummary category, BrandSummary brand,
            BigDecimal basePrice, BigDecimal salePrice, BigDecimal effectivePrice, Image primaryImage,
            Long defaultVariantId, int variantCount, long totalStock, boolean inStock,
            List<Variant> variants) {
        public static Summary from(Product product, Map<Long, Long> stockByVariant) {
            List<Variant> variants = product.getVariants().stream()
                    .filter(ProductVariant::isActive)
                    .map(variant -> Variant.from(variant, product.getEffectivePrice(), stockByVariant))
                    .toList();
            Image primaryImage = product.getImages().stream()
                    .filter(ProductImage::isPrimary).findFirst().map(Image::from).orElse(null);
            Long defaultVariantId = product.getVariants().stream()
                    .filter(ProductVariant::isDefault).map(ProductVariant::getId).findFirst().orElse(null);
            long totalStock = variants.stream().mapToLong(Variant::totalStock).sum();
            Brand brand = product.getBrand();
            return new Summary(product.getId(), product.getName(), product.getSlug(),
                    CategorySummary.from(product.getCategory()),
                    brand == null ? null : BrandSummary.from(brand), product.getBasePrice(),
                    product.getSalePrice(), product.getEffectivePrice(), primaryImage, defaultVariantId,
                    variants.size(), totalStock, totalStock > 0, variants);
        }
    }

    public record Detail(
            Long id, String name, String slug, String description, JsonNode specifications,
            CategorySummary category, BrandSummary brand, BigDecimal basePrice, BigDecimal salePrice,
            BigDecimal effectivePrice, Long defaultVariantId, int variantCount, long totalStock,
            boolean inStock, List<Image> images, List<Variant> variants, OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
        public static Detail from(Product product, Map<Long, Long> stockByVariant) {
            Summary summary = Summary.from(product, stockByVariant);
            return new Detail(summary.id(), summary.name(), summary.slug(), product.getDescription(),
                    product.getSpecifications(), summary.category(), summary.brand(), summary.basePrice(),
                    summary.salePrice(), summary.effectivePrice(), summary.defaultVariantId(),
                    summary.variantCount(), summary.totalStock(), summary.inStock(),
                    product.getImages().stream().map(Image::from).toList(), summary.variants(),
                    product.getCreatedAt(), product.getUpdatedAt());
        }
    }

    public record ProductPage(
            List<Summary> content, int page, int size, long totalElements, int totalPages,
            boolean first, boolean last, boolean empty) {
        public int number() {
            return page;
        }

        public boolean isEmpty() {
            return empty;
        }

        public static ProductPage from(Page<Product> products, Map<Long, Long> stockByVariant) {
            return new ProductPage(products.getContent().stream()
                    .map(product -> Summary.from(product, stockByVariant)).toList(),
                    products.getNumber(), products.getSize(), products.getTotalElements(),
                    products.getTotalPages(), products.isFirst(), products.isLast(), products.isEmpty());
        }
    }
}
