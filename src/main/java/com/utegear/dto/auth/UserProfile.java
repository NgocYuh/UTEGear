package com.utegear.dto.auth;

import com.utegear.entity.Role;
import com.utegear.entity.User;
import java.time.OffsetDateTime;
import java.util.List;

public record UserProfile(
        Long id,
        String fullName,
        String email,
        String phone,
        String avatarUrl,
        boolean enabled,
        List<String> roles,
        OffsetDateTime createdAt) {

    public static UserProfile from(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .sorted()
                .toList();
        return new UserProfile(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAvatarUrl(),
                user.isEnabled(),
                roleNames,
                user.getCreatedAt());
    }
}
