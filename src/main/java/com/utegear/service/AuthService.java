package com.utegear.service;

import com.utegear.dto.auth.RegisterRequest;
import com.utegear.dto.auth.UserProfile;
import com.utegear.entity.Role;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.RoleRepository;
import com.utegear.repository.UserRepository;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserProfile register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String phone = normalizePhone(request.phone());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "Email đã được sử dụng");
        }
        if (phone != null && userRepository.existsByPhone(phone)) {
            throw new ApiException(
                    HttpStatus.CONFLICT, "PHONE_ALREADY_EXISTS", "Số điện thoại đã được sử dụng");
        }

        Role customer = roleRepository.findByName(Role.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("Role CUSTOMER chưa được khởi tạo"));
        User user = new User(
                request.fullName().trim(),
                email,
                phone,
                passwordEncoder.encode(request.password()));
        user.getRoles().add(customer);
        return UserProfile.from(userRepository.save(user));
    }

    private String normalizePhone(String phone) {
        return phone == null || phone.isBlank() ? null : phone.trim();
    }
}
