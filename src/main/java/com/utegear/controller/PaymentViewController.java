package com.utegear.controller;

import com.utegear.dto.payment.VNPayCallbackResult;
import com.utegear.service.VNPayService;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/payment")
public class PaymentViewController {

    private final VNPayService vnPayService;

    public PaymentViewController(VNPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    @GetMapping("/vnpay-return")
    public String vnpayReturn(@RequestParam Map<String, String> allParams, Model model) {
        VNPayCallbackResult result = vnPayService.processReturn(allParams);
        model.addAttribute("result", result);
        model.addAttribute("responseMessage", vnPayService.getResponseMessage(result.responseCode()));
        return "payment/vnpay-return";
    }
}

