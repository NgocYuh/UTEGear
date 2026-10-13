package com.utegear.dto.wishlist;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WishlistItemRequest(
        @NotNull(message = "Sản phẩm là bắt buộc")
        @Positive(message = "Mã sản phẩm phải lớn hơn 0")
        Long productId) {
}
