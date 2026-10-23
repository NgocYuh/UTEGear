package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.dto.auth.AuthMessageResponse;
import com.utegear.dto.auth.ForgotPasswordRequest;
import com.utegear.dto.auth.RegisterRequest;
import com.utegear.dto.auth.RegisterResponse;
import com.utegear.dto.auth.ResendOtpRequest;
import com.utegear.dto.auth.ResetPasswordRequest;
import com.utegear.dto.auth.VerifyOtpRequest;
import com.utegear.entity.EmailOtp;
import com.utegear.entity.OtpType;
import com.utegear.entity.Role;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.EmailOtpRepository;
import com.utegear.repository.RoleRepository;
import com.utegear.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EmailOtpRepository emailOtpRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                roleRepository,
                emailOtpRepository,
                passwordEncoder,
                emailService);
    }

    @Test
    void registersCustomerWithNormalizedEmailAndEncodedPasswordAndSendsOtp() {
        RegisterRequest request = new RegisterRequest(
                "Nguyen Van An", " An@Example.com ", "0900000007", "password123");
        when(roleRepository.findByName(Role.CUSTOMER)).thenReturn(Optional.of(new Role(Role.CUSTOMER)));
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse response = authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("an@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(savedUser.isEnabled()).isFalse();
        assertThat(savedUser.getRoles()).extracting(Role::getName).containsExactly(Role.CUSTOMER);

        verify(emailOtpRepository).save(any(EmailOtp.class));
        verify(emailService).sendRegistrationOtp(eq("an@example.com"), eq("Nguyen Van An"), any(String.class));
        assertThat(response.email()).isEqualTo("an@example.com");
        assertThat(response.requiresActivation()).isTrue();
    }

    @Test
    void rejectsExistingEmail() {
        RegisterRequest request = new RegisterRequest(
                "Nguyen Van An", "an@example.com", null, "password123");
        User existing = new User("Nguyen Van An", "an@example.com", null, "hash");
        existing.setEnabled(true);
        when(userRepository.findByEmailIgnoreCase("an@example.com")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo("EMAIL_ALREADY_EXISTS");
                    assertThat(exception.getStatus().value()).isEqualTo(409);
                });
        verify(userRepository, never()).save(any());
        verify(emailOtpRepository, never()).save(any());
    }

    @Test
    void rejectsExistingPhone() {
        RegisterRequest request = new RegisterRequest(
                "Nguyen Van An", "an@example.com", "0900000007", "password123");
        when(userRepository.existsByPhone("0900000007")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getCode()).isEqualTo("PHONE_ALREADY_EXISTS"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void verifiesRegistrationOtpSuccessfully() {
        User user = new User("Nguyen Van An", "user@example.com", "0900000001", "password123");
        user.setEnabled(false);

        EmailOtp otp = new EmailOtp("user@example.com", "123456", OtpType.REGISTRATION_ACTIVATION, OffsetDateTime.now().plusMinutes(10));

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(emailOtpRepository.findTopByEmailIgnoreCaseAndOtpTypeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                eq("user@example.com"), eq(OtpType.REGISTRATION_ACTIVATION), any(OffsetDateTime.class)))
                .thenReturn(Optional.of(otp));

        AuthMessageResponse res = authService.verifyRegistrationOtp(new VerifyOtpRequest("user@example.com", "123456"));

        assertThat(user.isEnabled()).isTrue();
        assertThat(otp.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(emailOtpRepository).save(otp);
        assertThat(res.success()).isTrue();
    }

    @Test
    void failsWhenOtpIsExpiredOrInvalid() {
        User user = new User("Nguyen Van An", "user@example.com", "0900000001", "password123");
        user.setEnabled(false);

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(emailOtpRepository.findTopByEmailIgnoreCaseAndOtpTypeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                eq("user@example.com"), eq(OtpType.REGISTRATION_ACTIVATION), any(OffsetDateTime.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyRegistrationOtp(new VerifyOtpRequest("user@example.com", "123456")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("không hợp lệ hoặc đã hết hạn");
    }

    @Test
    void resendsRegistrationOtpForDisabledUser() {
        User user = new User("Nguyen Van An", "user@example.com", "0900000001", "password123");
        user.setEnabled(false);

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

        AuthMessageResponse res = authService.resendRegistrationOtp(new ResendOtpRequest("user@example.com"));

        verify(emailOtpRepository).save(any(EmailOtp.class));
        verify(emailService).sendRegistrationOtp(eq("user@example.com"), eq("Nguyen Van An"), any(String.class));
        assertThat(res.success()).isTrue();
    }

    @Test
    void forgotPasswordGeneratesResetOtpAndSendsEmail() {
        User user = new User("Nguyen Van An", "user@example.com", "0900000001", "password123");

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

        AuthMessageResponse res = authService.forgotPassword(new ForgotPasswordRequest("user@example.com"));

        verify(emailOtpRepository).save(argThat(otp -> otp.getOtpType() == OtpType.PASSWORD_RESET));
        verify(emailService).sendPasswordResetOtp(eq("user@example.com"), eq("Nguyen Van An"), any(String.class));
        assertThat(res.success()).isTrue();
    }

    @Test
    void resetsPasswordSuccessfullyWithValidOtp() {
        User user = new User("Nguyen Van An", "user@example.com", "0900000001", "old-hash");

        EmailOtp otp = new EmailOtp("user@example.com", "654321", OtpType.PASSWORD_RESET, OffsetDateTime.now().plusMinutes(10));

        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(emailOtpRepository.findTopByEmailIgnoreCaseAndOtpTypeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                eq("user@example.com"), eq(OtpType.PASSWORD_RESET), any(OffsetDateTime.class)))
                .thenReturn(Optional.of(otp));
        when(passwordEncoder.encode("newPassword123")).thenReturn("new-encoded-hash");

        AuthMessageResponse res = authService.resetPassword(new ResetPasswordRequest("user@example.com", "654321", "newPassword123"));

        assertThat(user.getPasswordHash()).isEqualTo("new-encoded-hash");
        assertThat(otp.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(emailOtpRepository).save(otp);
        assertThat(res.success()).isTrue();
    }
}
