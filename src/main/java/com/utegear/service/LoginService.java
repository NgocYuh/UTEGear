package com.utegear.service;

import com.utegear.dto.auth.AuthSession;
import com.utegear.dto.auth.LoginRequest;
import com.utegear.dto.auth.RefreshTokenRequest;
import com.utegear.dto.auth.UserProfile;
import com.utegear.entity.RefreshToken;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.security.CustomUserDetails;
import com.utegear.security.JwtService;
import java.time.Instant;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    public LoginService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            TokenBlacklistService tokenBlacklistService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Transactional
    public AuthSession login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
            CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
            User user = principal.getUser();

            String accessToken = jwtService.generateToken(principal);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            return new AuthSession(
                    accessToken,
                    "Bearer",
                    jwtService.extractExpiration(accessToken),
                    UserProfile.from(user),
                    refreshToken.getToken());
        } catch (DisabledException exception) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED", "Tài khoản chưa được kích hoạt hoặc đã bị vô hiệu hóa");
        } catch (AuthenticationException exception) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng");
        }
    }

    @Transactional
    public AuthSession refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.refreshToken());
        User user = refreshToken.getUser();

        if (!user.isEnabled()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED", "Tài khoản đã bị vô hiệu hóa");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = jwtService.generateToken(userDetails);

        return new AuthSession(
                newAccessToken,
                "Bearer",
                jwtService.extractExpiration(newAccessToken),
                UserProfile.from(user),
                refreshToken.getToken());
    }

    @Transactional
    public void logout(String authorizationHeader, String refreshToken) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7).trim();
            Instant expiration = null;
            try {
                expiration = jwtService.extractExpiration(token);
            } catch (Exception ignored) {
                // If token is malformed or expired, set a fallback expiration of 24 hours
                expiration = Instant.now().plusSeconds(86400);
            }
            tokenBlacklistService.blacklistToken(token, expiration);
        }

        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revokeToken(refreshToken);
        }
    }
}
