package com.utegear.service;

import com.utegear.entity.RefreshToken;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.RefreshTokenRepository;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenValidityDays;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh-token-expiration-days:7}") long refreshTokenValidityDays) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenValidityDays = refreshTokenValidityDays > 0 ? refreshTokenValidityDays : 7;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String token = UUID.randomUUID().toString().replace("-", "") + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusDays(refreshTokenValidityDays);
        RefreshToken refreshToken = new RefreshToken(user, token, expiresAt);
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String token) {
        if (token == null || token.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh token không hợp lệ");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedFalse(token.trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh token không hợp lệ hoặc đã bị thu hồi"));

        if (refreshToken.getExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "EXPIRED_REFRESH_TOKEN", "Refresh token đã hết hạn, vui lòng đăng nhập lại");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeToken(String token) {
        if (token != null && !token.isBlank()) {
            refreshTokenRepository.revokeByToken(token.trim());
        }
    }

    @Transactional
    public void revokeAllUserTokens(Long userId) {
        if (userId != null) {
            refreshTokenRepository.revokeAllByUserId(userId);
        }
    }
}

