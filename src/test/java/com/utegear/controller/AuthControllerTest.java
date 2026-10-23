package com.utegear.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utegear.dto.auth.AuthMessageResponse;
import com.utegear.dto.auth.AuthSession;
import com.utegear.dto.auth.ForgotPasswordRequest;
import com.utegear.dto.auth.LoginRequest;
import com.utegear.dto.auth.RegisterRequest;
import com.utegear.dto.auth.RegisterResponse;
import com.utegear.dto.auth.ResendOtpRequest;
import com.utegear.dto.auth.ResetPasswordRequest;
import com.utegear.dto.auth.UserProfile;
import com.utegear.dto.auth.VerifyOtpRequest;
import com.utegear.service.AuthService;
import com.utegear.service.LoginService;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private LoginService loginService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(authService, loginService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void registerReturnsCreatedAndRegisterResponse() throws Exception {
        RegisterRequest req = new RegisterRequest("Test User", "test@utegear.vn", "0901234567", "password123");
        RegisterResponse res = new RegisterResponse("test@utegear.vn", true, "Đăng ký thành công");

        when(authService.register(any(RegisterRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@utegear.vn"))
                .andExpect(jsonPath("$.requiresActivation").value(true));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void verifyRegistrationOtpReturnsSuccess() throws Exception {
        VerifyOtpRequest req = new VerifyOtpRequest("test@utegear.vn", "123456");
        AuthMessageResponse res = new AuthMessageResponse(true, "Kích hoạt tài khoản thành công!");

        when(authService.verifyRegistrationOtp(any(VerifyOtpRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/auth/verify-registration-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Kích hoạt tài khoản thành công!"));
    }

    @Test
    void resendRegistrationOtpReturnsSuccess() throws Exception {
        ResendOtpRequest req = new ResendOtpRequest("test@utegear.vn");
        AuthMessageResponse res = new AuthMessageResponse(true, "Mã kích hoạt mới đã được gửi!");

        when(authService.resendRegistrationOtp(any(ResendOtpRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/auth/resend-registration-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void forgotPasswordReturnsSuccess() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest("test@utegear.vn");
        AuthMessageResponse res = new AuthMessageResponse(true, "Mã OTP đặt lại mật khẩu đã được gửi!");

        when(authService.forgotPassword(any(ForgotPasswordRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void resetPasswordReturnsSuccess() throws Exception {
        ResetPasswordRequest req = new ResetPasswordRequest("test@utegear.vn", "123456", "newPassword123");
        AuthMessageResponse res = new AuthMessageResponse(true, "Đặt lại mật khẩu thành công!");

        when(authService.resetPassword(any(ResetPasswordRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void loginReturnsAuthSession() throws Exception {
        LoginRequest req = new LoginRequest("test@utegear.vn", "password123");
        UserProfile profile = new UserProfile(1L, "Test User", "test@utegear.vn", "0901234567", null, true, List.of("CUSTOMER"), OffsetDateTime.now());
        AuthSession session = new AuthSession("token123", "Bearer", Instant.now().plusSeconds(3600), profile);

        when(loginService.login(any(LoginRequest.class))).thenReturn(session);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token123"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("test@utegear.vn"));
    }

    @Test
    void logoutReturnsNoContent() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent());
    }
}

