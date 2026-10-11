package com.utegear.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.utegear.entity.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "test-secret-with-at-least-thirty-two-bytes";

    @Test
    void generatesAndValidatesToken() {
        JwtService jwtService = new JwtService(SECRET, 60_000);
        CustomUserDetails principal = principal("user@example.com");

        String token = jwtService.generateToken(principal);

        assertThat(jwtService.extractSubject(token)).isEqualTo("user@example.com");
        assertThat(jwtService.isValid(token, principal)).isTrue();
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        JwtService issuer = new JwtService(SECRET, 60_000);
        JwtService verifier = new JwtService("another-test-secret-with-thirty-two-bytes", 60_000);
        String token = issuer.generateToken(principal("user@example.com"));

        assertThatThrownBy(() -> verifier.extractSubject(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsExpiredToken() throws InterruptedException {
        JwtService jwtService = new JwtService(SECRET, 1);
        String token = jwtService.generateToken(principal("user@example.com"));
        Thread.sleep(20);

        assertThatThrownBy(() -> jwtService.extractSubject(token)).isInstanceOf(JwtException.class);
    }

    private CustomUserDetails principal(String email) {
        return new CustomUserDetails(new User("Test User", email, null, "encoded-password"));
    }
}
