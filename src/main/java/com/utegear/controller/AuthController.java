package com.utegear.controller;

import com.utegear.dto.auth.AuthMessageResponse;
import com.utegear.dto.auth.AuthSession;
import com.utegear.dto.auth.ForgotPasswordRequest;
import com.utegear.dto.auth.LoginRequest;
import com.utegear.dto.auth.RefreshTokenRequest;
import com.utegear.dto.auth.RegisterRequest;
import com.utegear.dto.auth.RegisterResponse;
import com.utegear.dto.auth.ResendOtpRequest;
import com.utegear.dto.auth.ResetPasswordRequest;
import com.utegear.dto.auth.VerifyOtpRequest;
import com.utegear.service.AuthService;
import com.utegear.service.LoginService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final LoginService loginService;

    public AuthController(AuthService authService, LoginService loginService) {
        this.authService = authService;
        this.loginService = loginService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/verify-registration-otp")
    public AuthMessageResponse verifyRegistrationOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return authService.verifyRegistrationOtp(request);
    }

    @PostMapping("/resend-registration-otp")
    public AuthMessageResponse resendRegistrationOtp(@Valid @RequestBody ResendOtpRequest request) {
        return authService.resendRegistrationOtp(request);
    }

    @PostMapping("/forgot-password")
    public AuthMessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public AuthMessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @PostMapping("/login")
    public AuthSession login(@Valid @RequestBody LoginRequest request) {
        return loginService.login(request);
    }

    @PostMapping("/refresh")
    public AuthSession refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return loginService.refreshToken(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) RefreshTokenRequest request) {
        String refreshToken = request != null ? request.refreshToken() : null;
        loginService.logout(authorization, refreshToken);
    }
}
