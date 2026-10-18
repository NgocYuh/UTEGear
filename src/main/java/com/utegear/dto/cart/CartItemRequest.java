package com.utegear.dto.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequest(
        @NotNull(message = "Biến thể là bắt buộc")
        @Min(value = 1, message = "ID biến thể phải là số dương")
        Long variantId,

        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        @Max(value = 99, message = "Số lượng không được vượt quá 99")
        int quantity) {
}
