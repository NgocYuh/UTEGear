package com.utegear.dto.order;

import com.utegear.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PlaceOrderRequest(
        @NotBlank String quoteToken,
        @NotNull Long addressId,
        @NotNull Long fulfillmentStoreId,
        @Size(max = 50) String couponCode,
        @NotNull PaymentMethod paymentMethod,
        @Size(max = 500) String note,
        @NotEmpty List<@NotNull @Valid CheckoutItemRequest> items) {
}
