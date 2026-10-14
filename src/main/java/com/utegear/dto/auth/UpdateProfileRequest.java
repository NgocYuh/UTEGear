package com.utegear.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Họ tên là bắt buộc")
        @Size(min = 2, max = 100, message = "Họ tên phải có từ 2 đến 100 ký tự")
        String fullName,

        @Pattern(regexp = "^$|^\\+?[0-9]{10,20}$", message = "Số điện thoại phải có từ 10 đến 20 chữ số")
        String phone) {
}
