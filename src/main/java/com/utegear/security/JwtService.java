package com.utegear.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMillis) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret phải có ít nhất 32 byte");
        }
        if (expirationMillis <= 0) {
            throw new IllegalArgumentException("JWT expiration phải lớn hơn 0");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(UserDetails userDetails) {
        Instant issuedAt = Instant.now();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plusMillis(expirationMillis)))
                .signWith(signingKey)
                .compact();
    }

    public String extractSubject(String token) {
        return claims(token).getSubject();
    }

    public Instant extractExpiration(String token) {
        return claims(token).getExpiration().toInstant();
    }

    public boolean isValid(String token, UserDetails userDetails) {
        Claims claims = claims(token);
        return claims.getSubject().equals(userDetails.getUsername())
                && claims.getExpiration().toInstant().isAfter(Instant.now());
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
