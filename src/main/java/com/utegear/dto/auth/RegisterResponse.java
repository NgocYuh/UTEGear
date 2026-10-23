package com.utegear.dto.auth;

public record RegisterResponse(
        String email,
        boolean requiresActivation,
        String message) {
}

