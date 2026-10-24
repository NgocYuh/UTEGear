package com.utegear.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class VNPayUtilTest {

    private static final String HASH_SECRET = "TEST_HASH_SECRET_123456";

    @Test
    void hmacSHA512ProducesConsistentHash() {
        String hash1 = VNPayUtil.hmacSHA512(HASH_SECRET, "test-data-for-vnpay");
        String hash2 = VNPayUtil.hmacSHA512(HASH_SECRET, "test-data-for-vnpay");

        assertThat(hash1).isNotBlank();
        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).hasSize(128); // SHA-512 hex is 128 characters
    }

    @Test
    void hashAllFieldsSortsAlphabeticallyAndCalculatesHash() {
        Map<String, String> fields = new HashMap<>();
        fields.put("vnp_Version", "2.1.0");
        fields.put("vnp_Command", "pay");
        fields.put("vnp_Amount", "1000000");

        String hash = VNPayUtil.hashAllFields(fields, HASH_SECRET);
        assertThat(hash).isNotBlank();
    }

    @Test
    void buildPaymentUrlGeneratesValidUrlWithSecureHash() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", "TESTTMN");
        params.put("vnp_Amount", "50000000");
        params.put("vnp_TxnRef", "UTE-ORDER-001");

        String url = VNPayUtil.buildPaymentUrl(params, "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html", HASH_SECRET);

        assertThat(url).startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?");
        assertThat(url).contains("vnp_TxnRef=UTE-ORDER-001");
        assertThat(url).contains("vnp_SecureHash=");
    }

    @Test
    void getClientIpAddressExtractsIpFromHeaderOrRemote() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18");
        assertThat(VNPayUtil.getClientIpAddress(request)).isEqualTo("203.0.113.195");

        MockHttpServletRequest requestRemote = new MockHttpServletRequest();
        requestRemote.setRemoteAddr("192.168.1.10");
        assertThat(VNPayUtil.getClientIpAddress(requestRemote)).isEqualTo("192.168.1.10");

        MockHttpServletRequest requestLocalhost = new MockHttpServletRequest();
        requestLocalhost.setRemoteAddr("0:0:0:0:0:0:0:1");
        assertThat(VNPayUtil.getClientIpAddress(requestLocalhost)).isEqualTo("127.0.0.1");
    }
}

