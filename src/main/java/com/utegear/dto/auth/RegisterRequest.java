package com.utegear.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Họ tên là bắt buộc")
        @Size(min = 2, max = 100, message = "Họ tên phải có từ 2 đến 100 ký tự")
        String fullName,

        @NotBlank(message = "Email là bắt buộc")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
        String email,

        @Pattern(regexp = "^$|^\\s*$|^(0|\\+84)[35789][0-9]{8}$", message = "Số điện thoại không hợp lệ (phải là số di động 10 chữ số của Việt Nam)")
        String phone,

        @NotBlank(message = "Mật khẩu là bắt buộc")
        @Size(min = 8, max = 72, message = "Mật khẩu phải có từ 8 đến 72 ký tự")
        String password) {
}
