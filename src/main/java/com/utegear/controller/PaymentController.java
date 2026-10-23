package com.utegear.controller;

import com.utegear.dto.payment.VNPayIpnResponse;
import com.utegear.dto.payment.VNPayPaymentUrlResponse;
import com.utegear.entity.Order;
import com.utegear.entity.PaymentMethod;
import com.utegear.entity.PaymentStatus;
import com.utegear.exception.ApiException;
import com.utegear.repository.OrderRepository;
import com.utegear.security.CustomUserDetails;
import com.utegear.service.VNPayService;
import com.utegear.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final VNPayService vnPayService;
    private final OrderRepository orderRepository;

    public PaymentController(VNPayService vnPayService, OrderRepository orderRepository) {
        this.vnPayService = vnPayService;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/vnpay/create-url")
    public VNPayPaymentUrlResponse createPaymentUrl(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam Long orderId,
            HttpServletRequest request) {
        if (principal == null || principal.getUser() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Vui lòng đăng nhập để thực hiện");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "Không tìm thấy đơn hàng"));

        if (!order.getUser().getId().equals(principal.getUser().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Bạn không có quyền truy cập đơn hàng này");
        }

        if (order.getPayment() == null || order.getPayment().getMethod() != PaymentMethod.VNPAY) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAYMENT_METHOD", "Đơn hàng không dùng phương thức VNPay");
        }

        if (order.getPayment().getStatus() != PaymentStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PAYMENT_NOT_PENDING", "Đơn hàng đã được thanh toán hoặc đã hủy");
        }

        String clientIp = VNPayUtil.getClientIpAddress(request);
        String paymentUrl = vnPayService.createPaymentUrl(order, clientIp);

        return new VNPayPaymentUrlResponse(paymentUrl, order.getOrderCode(), order.getTotal());
    }

    @GetMapping("/vnpay/ipn")
    public VNPayIpnResponse processIpn(@RequestParam Map<String, String> allParams) {
        return vnPayService.processIpn(allParams);
    }
}

