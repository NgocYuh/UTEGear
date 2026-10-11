package com.utegear.controller;

import com.utegear.dto.auth.UserProfile;
import com.utegear.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @GetMapping
    public UserProfile profile(@AuthenticationPrincipal CustomUserDetails principal) {
        return UserProfile.from(principal.getUser());
    }
}
