package com.utegear.service;

import com.utegear.dto.admin.AdminDtos;
import com.utegear.entity.Role;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.RoleRepository;
import com.utegear.repository.UserRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {
    private static final Set<String> ALLOWED_ROLES = Set.of(Role.ADMIN, Role.CUSTOMER);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AdminUserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminDtos.UserView> findAll(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw validation("Phân trang không hợp lệ");
        return userRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .stream().map(AdminDtos.UserView::from).toList();
    }

    @Transactional(readOnly = true)
    public AdminDtos.UserView find(Long id) {
        return AdminDtos.UserView.from(requireUser(id));
    }

    @Transactional
    public AdminDtos.UserView changeEnabled(Long id, boolean enabled, Long actorId) {
        if (id.equals(actorId) && !enabled) {
            throw validation("Không thể tự khóa tài khoản quản trị đang đăng nhập");
        }
        User user = requireUser(id);
        user.setEnabled(enabled);
        return AdminDtos.UserView.from(userRepository.save(user));
    }

    @Transactional
    public AdminDtos.UserView changeRoles(Long id, Set<String> requestedRoles, Long actorId) {
        if (requestedRoles == null || requestedRoles.isEmpty() || !ALLOWED_ROLES.containsAll(requestedRoles)) {
            throw validation("Vai trò chỉ được gồm CUSTOMER hoặc ADMIN và không được để trống");
        }
        if (id.equals(actorId) && !requestedRoles.contains(Role.ADMIN)) {
            throw validation("Không thể tự gỡ vai trò ADMIN của phiên đang đăng nhập");
        }
        User user = requireUser(id);
        Set<Role> roles = new LinkedHashSet<>();
        for (String name : requestedRoles) {
            roles.add(roleRepository.findByName(name).orElseThrow(() -> validation("Vai trò chưa được khởi tạo")));
        }
        user.getRoles().clear();
        user.getRoles().addAll(roles);
        return AdminDtos.UserView.from(userRepository.save(user));
    }

    private User requireUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Không tìm thấy tài khoản"));
    }

    private ApiException validation(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
    }
}
