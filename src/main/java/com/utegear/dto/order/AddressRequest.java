package com.utegear.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(min = 2, max = 100) String fullName,
        @NotBlank @Pattern(regexp = "^(0|\\+84)[35789][0-9]{8}$", message = "Số điện thoại không hợp lệ (phải là số di động 10 chữ số của Việt Nam)") String phone,
        @NotBlank @Size(max = 255) String street,
        @Size(max = 100) String ward,
        @Size(max = 100) String district,
        @NotBlank @Size(max = 100) String province,
        @Size(max = 100) String label) {

    public AddressCommand toCommand() {
        return new AddressCommand(fullName, phone, street, ward, district, province, label);
    }
}
