package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.dto.auth.AuthSession;
import com.utegear.dto.auth.LoginRequest;
import com.utegear.dto.auth.RefreshTokenRequest;
import com.utegear.entity.RefreshToken;
import com.utegear.entity.Role;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.security.CustomUserDetails;
import com.utegear.security.JwtService;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private JwtService jwtService;
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("test-secret-with-at-least-thirty-two-bytes", 60_000);
        loginService = new LoginService(authenticationManager, jwtService, refreshTokenService, tokenBlacklistService);
    }

    @Test
    void returnsBearerSessionWithRefreshTokenForValidCredentials() {
        User user = new User("Nguyen Van An", "an@example.com", null, "encoded-password");
        user.getRoles().add(new Role(Role.CUSTOMER));
        CustomUserDetails principal = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                "an@example.com", "password123"))).thenReturn(authentication);

        RefreshToken dummyRefresh = new RefreshToken(user, "dummy-refresh-token", OffsetDateTime.now().plusDays(7));
        when(refreshTokenService.createRefreshToken(user)).thenReturn(dummyRefresh);

        AuthSession session = loginService.login(
                new LoginRequest(" An@Example.com ", "password123"));

        assertThat(session.tokenType()).isEqualTo("Bearer");
        assertThat(jwtService.extractSubject(session.accessToken())).isEqualTo("an@example.com");
        assertThat(session.user().roles()).containsExactly(Role.CUSTOMER);
        assertThat(session.refreshToken()).isEqualTo("dummy-refresh-token");
    }

    @Test
    void mapsBadCredentialsToContractError() {
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                "an@example.com", "wrong-password")))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> loginService.login(
                new LoginRequest("an@example.com", "wrong-password")))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus().value()).isEqualTo(401);
                    assertThat(exception.getCode()).isEqualTo("INVALID_CREDENTIALS");
                });
    }

    @Test
    void mapsDisabledAccountToContractError() {
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                "an@example.com", "password123")))
                .thenThrow(new DisabledException("disabled"));

        assertThatThrownBy(() -> loginService.login(
                new LoginRequest("an@example.com", "password123")))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus().value()).isEqualTo(403);
                    assertThat(exception.getCode()).isEqualTo("ACCOUNT_DISABLED");
                });
    }

    @Test
    void refreshesAccessTokenSuccessfully() {
        User user = new User("Nguyen Van An", "an@example.com", null, "encoded-password");
        user.getRoles().add(new Role(Role.CUSTOMER));
        RefreshToken refreshToken = new RefreshToken(user, "valid-refresh-token", OffsetDateTime.now().plusDays(7));

        when(refreshTokenService.verifyRefreshToken("valid-refresh-token")).thenReturn(refreshToken);

        AuthSession session = loginService.refreshToken(new RefreshTokenRequest("valid-refresh-token"));

        assertThat(session.accessToken()).isNotBlank();
        assertThat(jwtService.extractSubject(session.accessToken())).isEqualTo("an@example.com");
        assertThat(session.refreshToken()).isEqualTo("valid-refresh-token");
    }

    @Test
    void logoutBlacklistsAccessTokenAndRevokesRefreshToken() {
        String token = jwtService.generateToken(new CustomUserDetails(new User("Test", "test@test.vn", null, "hash")));
        String authHeader = "Bearer " + token;

        loginService.logout(authHeader, "some-refresh-token");

        verify(tokenBlacklistService).blacklistToken(token, jwtService.extractExpiration(token));
        verify(refreshTokenService).revokeToken("some-refresh-token");
    }
}
