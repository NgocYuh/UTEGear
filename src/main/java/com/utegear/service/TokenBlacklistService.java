package com.utegear.service;

import com.utegear.entity.TokenBlacklist;
import com.utegear.repository.TokenBlacklistRepository;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final Map<String, Instant> cache = new ConcurrentHashMap<>();

    public TokenBlacklistService(TokenBlacklistRepository tokenBlacklistRepository) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @Transactional
    public void blacklistToken(String token, Instant expiresAt) {
        if (token == null || token.isBlank()) {
            return;
        }
        Instant exp = expiresAt != null ? expiresAt : Instant.now().plusSeconds(86400);
        cache.put(token, exp);

        try {
            OffsetDateTime offsetExp = exp.atOffset(ZoneOffset.UTC);
            tokenBlacklistRepository.save(new TokenBlacklist(token, offsetExp));
        } catch (Exception ignored) {
            // Token might already be blacklisted or unique constraint violation
        }
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        Instant cachedExpiry = cache.get(token);
        if (cachedExpiry != null) {
            if (cachedExpiry.isAfter(Instant.now())) {
                return true;
            }
            cache.remove(token);
            return false;
        }

        try {
            boolean exists = tokenBlacklistRepository.existsByToken(token);
            if (exists) {
                cache.put(token, Instant.now().plusSeconds(3600));
            }
            return exists;
        } catch (Exception e) {
            return false;
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            tokenBlacklistRepository.deleteByExpiresAtBefore(now);
        } catch (Exception ignored) {
        }
        cache.entrySet().removeIf(entry -> entry.getValue().isBefore(Instant.now()));
    }
}
