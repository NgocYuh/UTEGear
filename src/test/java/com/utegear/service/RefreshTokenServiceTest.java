package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.entity.RefreshToken;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.RefreshTokenRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, 7);
    }

    @Test
    void createsRefreshTokenWithValidity() {
        User user = new User("Nguyen Van An", "an@example.com", null, "hash");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken token = refreshTokenService.createRefreshToken(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getToken()).isNotBlank();
        assertThat(captor.getValue().getUser()).isSameAs(user);
        assertThat(token.getToken()).isNotBlank();
    }

    @Test
    void verifiesValidRefreshToken() {
        User user = new User("Nguyen Van An", "an@example.com", null, "hash");
        RefreshToken token = new RefreshToken(user, "valid-token", OffsetDateTime.now().plusDays(5));

        when(refreshTokenRepository.findByTokenAndRevokedFalse("valid-token")).thenReturn(Optional.of(token));

        RefreshToken verified = refreshTokenService.verifyRefreshToken("valid-token");
        assertThat(verified).isSameAs(token);
    }

    @Test
    void failsWhenTokenIsExpired() {
        User user = new User("Nguyen Van An", "an@example.com", null, "hash");
        RefreshToken token = new RefreshToken(user, "expired-token", OffsetDateTime.now().minusDays(1));

        when(refreshTokenRepository.findByTokenAndRevokedFalse("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken("expired-token"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Refresh token đã hết hạn");
    }

    @Test
    void failsWhenTokenNotFoundOrRevoked() {
        when(refreshTokenRepository.findByTokenAndRevokedFalse("revoked-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.verifyRefreshToken("revoked-token"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("không hợp lệ hoặc đã bị thu hồi");
    }

    @Test
    void revokesToken() {
        refreshTokenService.revokeToken("token-to-revoke");
        verify(refreshTokenRepository).revokeByToken("token-to-revoke");
    }

    @Test
    void revokesAllUserTokens() {
        refreshTokenService.revokeAllUserTokens(123L);
        verify(refreshTokenRepository).revokeAllByUserId(123L);
    }
}

