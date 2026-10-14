package com.utegear.controller;

import com.utegear.dto.order.AdminOrderDtos;
import com.utegear.entity.OrderStatus;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminViewController {

    @GetMapping("/admin")
    public String admin() {
        return "redirect:/admin/orders";
    }

    @GetMapping("/admin/orders")
    public String orders(@RequestParam(required = false) OrderStatus status, Model model) {
        model.addAttribute("orders", new AdminOrderDtos.OrderPage(List.of(), 0, 20, 0, 0));
        model.addAttribute("allStatuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("currentUser", null);
        model.addAttribute("adminPage", "orders");
        return "admin/orders/list";
    }

    @GetMapping("/admin/orders/{orderId}")
    public String orderDetail(@PathVariable Long orderId) {
        return "redirect:/admin/orders?focus=" + orderId;
    }
}
