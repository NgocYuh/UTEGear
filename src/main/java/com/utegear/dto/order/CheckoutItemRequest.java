package com.utegear.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CheckoutItemRequest(
        @NotNull Long variantId,
        @Min(1) int quantity) {
}
