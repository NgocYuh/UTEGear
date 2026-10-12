package com.utegear.dto.cart;

import com.utegear.entity.Cart;
import com.utegear.entity.CartItem;
import com.utegear.entity.Product;
import com.utegear.entity.ProductImage;
import com.utegear.entity.ProductStatus;
import com.utegear.entity.ProductVariant;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public record CartResponse(
        Long id,
        List<Item> items,
        int itemCount,
        BigDecimal subtotal,
        OffsetDateTime updatedAt) {

    public static CartResponse from(Cart cart, Map<Long, Long> stockByVariant) {
        List<Item> items = cart.getItems().stream()
                .map(item -> Item.from(item, stockByVariant.getOrDefault(item.getVariant().getId(), 0L)))
                .toList();
        return new CartResponse(cart.getId(), items, cart.getItemCount(), cart.getSubtotal(), cart.getUpdatedAt());
    }

    public record Item(
            Long id,
            Long variantId,
            int quantity,
            ProductSummary product,
            VariantSummary variant,
            BigDecimal unitPrice,
            BigDecimal lineTotal,
            long availableStock,
            boolean available) {

        private static Item from(CartItem item, long availableStock) {
            ProductVariant variant = item.getVariant();
            Product product = variant.getProduct();
            return new Item(
                    item.getId(),
                    variant.getId(),
                    item.getQuantity(),
                    ProductSummary.from(product),
                    VariantSummary.from(variant),
                    item.getUnitPrice(),
                    item.getLineTotal(),
                    availableStock,
                    variant.isActive()
                            && product.getStatus() == ProductStatus.ACTIVE
                            && availableStock >= item.getQuantity());
        }
    }

    public record ProductSummary(Long id, String name, String slug, PrimaryImage primaryImage) {

        private static ProductSummary from(Product product) {
            PrimaryImage image = product.getImages().stream()
                    .filter(ProductImage::isPrimary)
                    .findFirst()
                    .map(PrimaryImage::from)
                    .orElse(null);
            return new ProductSummary(product.getId(), product.getName(), product.getSlug(), image);
        }
    }

    public record PrimaryImage(String secureUrl, String altText) {

        private static PrimaryImage from(ProductImage image) {
            return new PrimaryImage(image.getSecureUrl(), image.getAltText());
        }
    }

    public record VariantSummary(
            String sku,
            Map<String, String> optionValues,
            boolean isDefault) {

        private static VariantSummary from(ProductVariant variant) {
            return new VariantSummary(variant.getSku(), variant.getOptionValues(), variant.isDefault());
        }
    }
}
