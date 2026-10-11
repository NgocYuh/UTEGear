package com.utegear.dto.store;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalTime;

public record StoreUpsertRequest(
        @NotBlank @Size(max = 200) String name,
        @NotBlank @Size(max = 255) String address,
        @NotBlank @Size(max = 100) String province,
        @Size(max = 100) String district,
        @Size(max = 20) String phone,
        @Email @Size(max = 100) String email,
        @Size(max = 500) String imageUrl,
        @NotNull LocalTime openingTime,
        @NotNull LocalTime closingTime,
        String description,
        @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude,
        @NotNull Boolean active) {
}
