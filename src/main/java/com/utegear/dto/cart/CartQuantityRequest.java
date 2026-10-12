package com.utegear.dto.cart;

import jakarta.validation.constraints.Min;

public record CartQuantityRequest(
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        int quantity) {
}
