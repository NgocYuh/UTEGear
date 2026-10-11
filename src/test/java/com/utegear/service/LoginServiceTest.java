package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.utegear.dto.auth.AuthSession;
import com.utegear.dto.auth.LoginRequest;
import com.utegear.entity.Role;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.security.CustomUserDetails;
import com.utegear.security.JwtService;
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

    private JwtService jwtService;
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("test-secret-with-at-least-thirty-two-bytes", 60_000);
        loginService = new LoginService(authenticationManager, jwtService);
    }

    @Test
    void returnsBearerSessionForValidCredentials() {
        User user = new User("Nguyen Van An", "an@example.com", null, "encoded-password");
        user.getRoles().add(new Role(Role.CUSTOMER));
        CustomUserDetails principal = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                "an@example.com", "password123"))).thenReturn(authentication);

        AuthSession session = loginService.login(
                new LoginRequest(" An@Example.com ", "password123"));

        assertThat(session.tokenType()).isEqualTo("Bearer");
        assertThat(jwtService.extractSubject(session.accessToken())).isEqualTo("an@example.com");
        assertThat(session.user().roles()).containsExactly(Role.CUSTOMER);
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
}
