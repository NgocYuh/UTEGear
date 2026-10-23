package com.utegear.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {

    @Value("${vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String payUrl;

    @Value("${vnpay.tmn-code:CGXZBACZ}")
    private String tmnCode;

    @Value("${vnpay.hash-secret:XNBCJFAKAZQSGTARRLKNOBZTTCRIWAKN}")
    private String hashSecret;

    @Value("${vnpay.return-url:http://localhost:8080/payment/vnpay-return}")
    private String returnUrl;

    public String getPayUrl() {
        return payUrl;
    }

    public String getTmnCode() {
        return tmnCode;
    }

    public String getHashSecret() {
        return hashSecret;
    }

    public String getReturnUrl() {
        return returnUrl;
    }
}

