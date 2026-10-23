package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.entity.TokenBlacklist;
import com.utegear.repository.TokenBlacklistRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private TokenBlacklistRepository tokenBlacklistRepository;

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService(tokenBlacklistRepository);
    }

    @Test
    void blacklistsTokenAndReturnsTrue() {
        tokenBlacklistService.blacklistToken("sample-token", Instant.now().plusSeconds(3600));

        verify(tokenBlacklistRepository).save(any(TokenBlacklist.class));
        assertThat(tokenBlacklistService.isBlacklisted("sample-token")).isTrue();
    }

    @Test
    void returnsFalseForUnblacklistedToken() {
        when(tokenBlacklistRepository.existsByToken("clean-token")).thenReturn(false);

        assertThat(tokenBlacklistService.isBlacklisted("clean-token")).isFalse();
    }

    @Test
    void returnsTrueWhenTokenExistsInRepository() {
        when(tokenBlacklistRepository.existsByToken("repo-token")).thenReturn(true);

        assertThat(tokenBlacklistService.isBlacklisted("repo-token")).isTrue();
    }
}

