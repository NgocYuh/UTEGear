package com.utegear.dto.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CartQuantityRequest(
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        @Max(value = 99, message = "Số lượng không được vượt quá 99")
        int quantity) {
}
