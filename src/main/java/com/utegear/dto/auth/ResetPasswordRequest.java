package com.utegear.dto.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Email là bắt buộc")
        @Email(message = "Email không đúng định dạng")
        String email,

        @NotBlank(message = "Mã OTP là bắt buộc")
        @Pattern(regexp = "^[0-9]{6}$", message = "Mã OTP phải gồm 6 chữ số")
        String otp,

        @NotBlank(message = "Mật khẩu mới là bắt buộc")
        @Size(min = 8, max = 72, message = "Mật khẩu mới phải từ 8 đến 72 ký tự")
        String newPassword) {

    @JsonCreator
    public static ResetPasswordRequest of(
            @JsonProperty("email") String email,
            @JsonProperty("otp") String otp,
            @JsonProperty("otpCode") String otpCode,
            @JsonProperty("otp_code") String otp_code,
            @JsonProperty("newPassword") String newPassword) {
        String resolvedOtp = (otp != null && !otp.isBlank()) ? otp
                : (otpCode != null && !otpCode.isBlank()) ? otpCode : otp_code;
        return new ResetPasswordRequest(email, resolvedOtp, newPassword);
    }
}
