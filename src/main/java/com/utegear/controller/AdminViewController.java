package com.utegear.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AdminViewController {

    @GetMapping("/admin")
    public String admin(Model model) {
        return view(model, "dashboard", "Tổng quan", "admin/dashboard");
    }

    @GetMapping("/admin/orders")
    public String orders(Model model) {
        return view(model, "orders", "Quản lý đơn hàng", "admin/orders/list");
    }

    @GetMapping("/admin/orders/{orderId}")
    public String orderDetail(@PathVariable Long orderId) {
        return "redirect:/admin/orders?focus=" + orderId;
    }

    @GetMapping({"/admin/products", "/admin/products/new", "/admin/products/{id}/edit"})
    public String products(Model model) {
        return view(model, "products", "Quản lý sản phẩm", "admin/products/list");
    }

    @GetMapping("/admin/categories")
    public String categories(Model model) {
        return view(model, "categories", "Quản lý danh mục", "admin/categories/list");
    }

    @GetMapping("/admin/brands")
    public String brands(Model model) {
        return view(model, "brands", "Quản lý thương hiệu", "admin/brands/list");
    }

    @GetMapping({"/admin/stores", "/admin/stores/{id}"})
    public String stores(Model model) {
        return view(model, "stores", "Quản lý cửa hàng và kho", "admin/stores/list");
    }

    @GetMapping("/admin/coupons")
    public String coupons(Model model) {
        return view(model, "coupons", "Quản lý mã giảm giá", "admin/coupons/list");
    }

    @GetMapping({"/admin/users", "/admin/users/{id}"})
    public String users(Model model) {
        return view(model, "users", "Quản lý tài khoản", "admin/users/list");
    }

    private String view(Model model, String page, String title, String template) {
        model.addAttribute("adminPage", page);
        model.addAttribute("pageTitle", title);
        return template;
    }
}
