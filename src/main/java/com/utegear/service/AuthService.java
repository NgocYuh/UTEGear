package com.utegear.service;

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
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            EmailOtpRepository emailOtpRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.emailOtpRepository = emailOtpRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String phone = normalizePhone(request.phone());

        Optional<User> existingUserOpt = userRepository.findByEmailIgnoreCase(email);
        User user;

        if (existingUserOpt.isPresent()) {
            User existing = existingUserOpt.get();
            if (existing.isEnabled()) {
                throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "Email đã được sử dụng");
            }
            if (phone != null && userRepository.existsByPhone(phone) && (existing.getPhone() == null || !existing.getPhone().equals(phone))) {
                throw new ApiException(HttpStatus.CONFLICT, "PHONE_ALREADY_EXISTS", "Số điện thoại đã được sử dụng");
            }
            existing.setFullName(request.fullName().trim());
            existing.setPhone(phone);
            existing.setPasswordHash(passwordEncoder.encode(request.password()));
            user = userRepository.save(existing);
        } else {
            if (phone != null && userRepository.existsByPhone(phone)) {
                throw new ApiException(HttpStatus.CONFLICT, "PHONE_ALREADY_EXISTS", "Số điện thoại đã được sử dụng");
            }
            Role customer = roleRepository.findByName(Role.CUSTOMER)
                    .orElseThrow(() -> new IllegalStateException("Role CUSTOMER chưa được khởi tạo"));
            user = new User(
                    request.fullName().trim(),
                    email,
                    phone,
                    passwordEncoder.encode(request.password()));
            user.setEnabled(false);
            user.getRoles().add(customer);
            user = userRepository.save(user);
        }

        String otpCode = generateOtp();
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5);
        emailOtpRepository.save(new EmailOtp(email, otpCode, OtpType.REGISTRATION_ACTIVATION, expiresAt));
        emailService.sendRegistrationOtp(email, user.getFullName(), otpCode);

        return new RegisterResponse(
                email,
                true,
                "Mã OTP kích hoạt tài khoản đã được gửi đến email " + email + ". Vui lòng nhập mã để hoàn tất đăng ký.");
    }

    @Transactional
    public AuthMessageResponse verifyRegistrationOtp(VerifyOtpRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String otp = request.otp().trim();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Không tìm thấy tài khoản với email này"));

        if (user.isEnabled()) {
            return new AuthMessageResponse(true, "Tài khoản của bạn đã được kích hoạt trước đó. Bạn có thể đăng nhập ngay.");
        }

        EmailOtp emailOtp = emailOtpRepository
                .findTopByEmailIgnoreCaseAndOtpTypeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        email, OtpType.REGISTRATION_ACTIVATION, OffsetDateTime.now(ZoneOffset.UTC))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "OTP_EXPIRED_OR_INVALID", "Mã OTP không hợp lệ hoặc đã hết hạn"));

        if (!emailOtp.getOtpCode().equals(otp)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OTP", "Mã OTP không chính xác");
        }

        emailOtp.setUsed(true);
        emailOtpRepository.save(emailOtp);

        user.setEnabled(true);
        userRepository.save(user);

        return new AuthMessageResponse(true, "Kích hoạt tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ.");
    }

    @Transactional
    public AuthMessageResponse resendRegistrationOtp(ResendOtpRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Không tìm thấy tài khoản với email này"));

        if (user.isEnabled()) {
            return new AuthMessageResponse(true, "Tài khoản của bạn đã được kích hoạt trước đó.");
        }

        String otpCode = generateOtp();
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5);
        emailOtpRepository.save(new EmailOtp(email, otpCode, OtpType.REGISTRATION_ACTIVATION, expiresAt));
        emailService.sendRegistrationOtp(email, user.getFullName(), otpCode);

        return new AuthMessageResponse(true, "Mã OTP kích hoạt mới đã được gửi đến email của bạn.");
    }

    @Transactional
    public AuthMessageResponse forgotPassword(ForgotPasswordRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isPresent() && userOpt.get().isEnabled()) {
            User user = userOpt.get();
            String otpCode = generateOtp();
            OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(10);
            emailOtpRepository.save(new EmailOtp(email, otpCode, OtpType.PASSWORD_RESET, expiresAt));
            emailService.sendPasswordResetOtp(email, user.getFullName(), otpCode);
        }

        return new AuthMessageResponse(true, "Nếu email của bạn tồn tại trong hệ thống, mã OTP đặt lại mật khẩu đã được gửi đến hộp thư.");
    }

    @Transactional
    public AuthMessageResponse resetPassword(ResetPasswordRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String otp = request.otp().trim();
        String newPassword = request.newPassword();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Không tìm thấy tài khoản với email này"));

        EmailOtp emailOtp = emailOtpRepository
                .findTopByEmailIgnoreCaseAndOtpTypeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                        email, OtpType.PASSWORD_RESET, OffsetDateTime.now(ZoneOffset.UTC))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "OTP_EXPIRED_OR_INVALID", "Mã OTP không hợp lệ hoặc đã hết hạn"));

        if (!emailOtp.getOtpCode().equals(otp)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OTP", "Mã OTP không chính xác");
        }

        emailOtp.setUsed(true);
        emailOtpRepository.save(emailOtp);

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return new AuthMessageResponse(true, "Đặt lại mật khẩu thành công! Vui lòng đăng nhập bằng mật khẩu mới.");
    }

    private String generateOtp() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }

    private String normalizePhone(String phone) {
        return phone == null || phone.isBlank() ? null : phone.trim();
    }
}
