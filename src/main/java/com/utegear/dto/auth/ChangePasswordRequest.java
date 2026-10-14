package com.utegear.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Mật khẩu hiện tại là bắt buộc")
        String currentPassword,

        @NotBlank(message = "Mật khẩu mới là bắt buộc")
        @Size(min = 8, max = 72, message = "Mật khẩu mới phải có từ 8 đến 72 ký tự")
        String newPassword,

        @NotBlank(message = "Xác nhận mật khẩu là bắt buộc")
        String confirmPassword) {
}
