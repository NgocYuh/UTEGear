package com.utegear.controller;

import com.utegear.dto.admin.AdminDtos;
import com.utegear.service.AdminDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {
    private final AdminDashboardService service;

    public AdminDashboardController(AdminDashboardService service) { this.service = service; }

    @GetMapping
    public AdminDtos.DashboardSummary summary() { return service.summary(); }
}
