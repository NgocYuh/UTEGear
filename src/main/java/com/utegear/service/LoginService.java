package com.utegear.service;

import com.utegear.dto.auth.AuthSession;
import com.utegear.dto.auth.LoginRequest;
import com.utegear.dto.auth.UserProfile;
import com.utegear.exception.ApiException;
import com.utegear.security.CustomUserDetails;
import com.utegear.security.JwtService;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthSession login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
            CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(principal);
            return new AuthSession(
                    token,
                    "Bearer",
                    jwtService.extractExpiration(token),
                    UserProfile.from(principal.getUser()));
        } catch (DisabledException exception) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED", "Tài khoản đã bị vô hiệu hóa");
        } catch (AuthenticationException exception) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng");
        }
    }
}
