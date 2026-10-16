package com.utegear.controller;

import com.utegear.dto.admin.AdminDtos;
import com.utegear.security.CustomUserDetails;
import com.utegear.service.AdminUserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AdminUserService service;

    public AdminUserController(AdminUserService service) { this.service = service; }

    @GetMapping
    public List<AdminDtos.UserView> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return service.findAll(page, size);
    }

    @GetMapping("/{id}")
    public AdminDtos.UserView find(@PathVariable Long id) { return service.find(id); }

    @PatchMapping("/{id}/enabled")
    public AdminDtos.UserView changeEnabled(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody AdminDtos.UserEnabledRequest request) {
        return service.changeEnabled(id, request.enabled(), principal.getUser().getId());
    }

    @PutMapping("/{id}/roles")
    public AdminDtos.UserView changeRoles(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody AdminDtos.UserRolesRequest request) {
        return service.changeRoles(id, request.roles(), principal.getUser().getId());
    }

    @PutMapping("/{id}/assigned-store")
    public AdminDtos.UserView changeAssignedStore(
            @PathVariable Long id,
            @Valid @RequestBody AdminDtos.UserStoreAssignmentRequest request) {
        return service.changeAssignedStore(id, request.storeId());
    }
}
