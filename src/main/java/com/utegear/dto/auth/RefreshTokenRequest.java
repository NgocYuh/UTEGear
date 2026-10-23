package com.utegear.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token là bắt buộc")
        @JsonAlias("refresh_token")
        String refreshToken) {
}

