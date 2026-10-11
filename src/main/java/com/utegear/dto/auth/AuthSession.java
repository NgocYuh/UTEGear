package com.utegear.dto.auth;

import java.time.Instant;

public record AuthSession(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        UserProfile user) {
}
