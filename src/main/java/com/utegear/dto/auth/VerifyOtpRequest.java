package com.utegear.dto.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(
        @NotBlank(message = "Email là bắt buộc")
        @Email(message = "Email không đúng định dạng")
        String email,

        @NotBlank(message = "Mã OTP là bắt buộc")
        @Pattern(regexp = "^[0-9]{6}$", message = "Mã OTP phải gồm 6 chữ số")
        String otp) {

    @JsonCreator
    public static VerifyOtpRequest of(
            @JsonProperty("email") String email,
            @JsonProperty("otp") String otp,
            @JsonProperty("otpCode") String otpCode,
            @JsonProperty("otp_code") String otp_code) {
        String resolvedOtp = (otp != null && !otp.isBlank()) ? otp
                : (otpCode != null && !otpCode.isBlank()) ? otpCode : otp_code;
        return new VerifyOtpRequest(email, resolvedOtp);
    }
}
