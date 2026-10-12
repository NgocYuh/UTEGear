package com.utegear.controller;

import com.utegear.dto.notification.NotificationPage;
import com.utegear.dto.notification.NotificationResponse;
import com.utegear.security.CustomUserDetails;
import com.utegear.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public NotificationPage list(@AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.list(principal.getUser(), read, page, size);
    }

    @PutMapping("/{notificationId}/read")
    public NotificationResponse markRead(@AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long notificationId) {
        return service.markRead(principal.getUser(), notificationId);
    }

    @PutMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(@AuthenticationPrincipal CustomUserDetails principal) {
        service.markAllRead(principal.getUser());
    }
}
