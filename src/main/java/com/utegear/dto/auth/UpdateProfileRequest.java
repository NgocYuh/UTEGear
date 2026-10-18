package com.utegear.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Họ tên là bắt buộc")
        @Size(min = 2, max = 100, message = "Họ tên phải có từ 2 đến 100 ký tự")
        String fullName,

        @Pattern(regexp = "^$|^\\s*$|^(0|\\+84)[35789][0-9]{8}$", message = "Số điện thoại không hợp lệ (phải là số di động 10 chữ số của Việt Nam)")
        String phone) {
}
