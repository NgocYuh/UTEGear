package com.utegear.dto.media;

import com.utegear.entity.ProductImage;

public record ProductImageResponse(
        Long id,
        Long productId,
        String secureUrl,
        String publicId,
        String altText,
        boolean primary,
        int sortOrder) {

    public static ProductImageResponse from(ProductImage image) {
        return new ProductImageResponse(
                image.getId(), image.getProduct().getId(), image.getSecureUrl(), image.getPublicId(),
                image.getAltText(), image.isPrimary(), image.getSortOrder());
    }
}
