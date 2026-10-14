package com.utegear.service;

import com.utegear.dto.auth.ChangePasswordRequest;
import com.utegear.dto.auth.UpdateProfileRequest;
import com.utegear.dto.auth.UserProfile;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserProfile get(User principalUser) {
        return UserProfile.from(requireCurrent(principalUser));
    }

    @Transactional
    public UserProfile update(User principalUser, UpdateProfileRequest request) {
        User user = requireCurrent(principalUser);
        String phone = normalize(request.phone());
        if (phone != null && users.existsByPhoneAndIdNot(phone, user.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "PHONE_ALREADY_EXISTS",
                    "Số điện thoại đã được sử dụng");
        }
        user.setFullName(request.fullName().trim());
        user.setPhone(phone);
        return UserProfile.from(users.save(user));
    }

    @Transactional
    public void changePassword(User principalUser, ChangePasswordRequest request) {
        User user = requireCurrent(principalUser);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CURRENT_PASSWORD_INVALID",
                    "Mật khẩu hiện tại không đúng");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                    "Xác nhận mật khẩu không khớp");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PASSWORD_UNCHANGED",
                    "Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        users.save(user);
    }

    private User requireCurrent(User principalUser) {
        if (principalUser == null || principalUser.getId() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Cần đăng nhập để tiếp tục");
        }
        return users.findById(principalUser.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                        "Tài khoản không còn tồn tại"));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
