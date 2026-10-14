package com.utegear.controller;

import com.utegear.dto.auth.ChangePasswordRequest;
import com.utegear.dto.auth.UpdateProfileRequest;
import com.utegear.dto.auth.UserProfile;
import com.utegear.security.CustomUserDetails;
import com.utegear.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public UserProfile profile(@AuthenticationPrincipal CustomUserDetails principal) {
        return profileService.get(principal.getUser());
    }

    @PatchMapping
    public UserProfile update(@AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        return profileService.update(principal.getUser(), request);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(principal.getUser(), request);
    }
}
