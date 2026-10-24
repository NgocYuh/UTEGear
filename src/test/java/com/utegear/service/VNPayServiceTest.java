package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.config.VNPayConfig;
import com.utegear.dto.payment.VNPayCallbackResult;
import com.utegear.dto.payment.VNPayIpnResponse;
import com.utegear.entity.Address;
import com.utegear.entity.Order;
import com.utegear.entity.OrderStatus;
import com.utegear.entity.Payment;
import com.utegear.entity.PaymentMethod;
import com.utegear.entity.PaymentStatus;
import com.utegear.entity.ShippingMethod;
import com.utegear.entity.Store;
import com.utegear.entity.User;
import com.utegear.repository.OrderRepository;
import com.utegear.repository.PaymentRepository;
import com.utegear.util.VNPayUtil;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class VNPayServiceTest {

    private static final String TMN_CODE = "CGXZBACZ";
    private static final String HASH_SECRET = "TEST_SECRET_HASH_KEY_123456789";
    private static final String PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String RETURN_URL = "http://localhost:8080/payment/vnpay-return";

    @Mock
    private VNPayConfig vnPayConfig;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    private VNPayService vnPayService;

    @BeforeEach
    void setUp() {
        lenient().when(vnPayConfig.getTmnCode()).thenReturn(TMN_CODE);
        lenient().when(vnPayConfig.getHashSecret()).thenReturn(HASH_SECRET);
        lenient().when(vnPayConfig.getPayUrl()).thenReturn(PAY_URL);
        lenient().when(vnPayConfig.getReturnUrl()).thenReturn(RETURN_URL);

        vnPayService = new VNPayService(vnPayConfig, orderRepository, paymentRepository);
    }

    @Test
    void createPaymentUrlBuildsValidSandboxUrl() {
        Order order = createOrder("UTE-ORD-12345", new BigDecimal("500000.00"));

        String url = vnPayService.createPaymentUrl(order, "127.0.0.1");

        assertThat(url).isNotBlank();
        assertThat(url).startsWith(PAY_URL + "?");
        assertThat(url).contains("vnp_Amount=50000000"); // 500.000 * 100
        assertThat(url).contains("vnp_TmnCode=" + TMN_CODE);
        assertThat(url).contains("vnp_TxnRef=UTE-ORD-12345");
        assertThat(url).contains("vnp_SecureHash=");
    }

    @Test
    void processReturnMarksPaymentPaidWhenResponseCodeIs00() {
        Order order = createOrder("UTE-ORD-12345", new BigDecimal("500000.00"));
        when(orderRepository.findByOrderCode("UTE-ORD-12345")).thenReturn(Optional.of(order));

        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "UTE-ORD-12345");
        params.put("vnp_Amount", "50000000");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionNo", "14890001");
        params.put("vnp_BankCode", "NCB");

        String hash = VNPayUtil.hashAllFields(params, HASH_SECRET);
        params.put("vnp_SecureHash", hash);

        VNPayCallbackResult result = vnPayService.processReturn(params);

        assertThat(result.success()).isTrue();
        assertThat(result.orderCode()).isEqualTo("UTE-ORD-12345");
        assertThat(result.transactionNo()).isEqualTo("14890001");
        assertThat(result.amount()).isEqualByComparingTo("500000.00");
        assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        verify(paymentRepository).save(order.getPayment());
        verify(orderRepository).save(order);
    }

    @Test
    void processReturnMarksPaymentFailedWhenResponseCodeIsNot00() {
        Order order = createOrder("UTE-ORD-12345", new BigDecimal("500000.00"));
        when(orderRepository.findByOrderCode("UTE-ORD-12345")).thenReturn(Optional.of(order));

        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "UTE-ORD-12345");
        params.put("vnp_Amount", "50000000");
        params.put("vnp_ResponseCode", "24"); // Khách hàng hủy giao dịch
        params.put("vnp_TransactionNo", "0");
        params.put("vnp_BankCode", "NCB");

        String hash = VNPayUtil.hashAllFields(params, HASH_SECRET);
        params.put("vnp_SecureHash", hash);

        VNPayCallbackResult result = vnPayService.processReturn(params);

        assertThat(result.success()).isFalse();
        assertThat(result.responseCode()).isEqualTo("24");
        assertThat(result.message()).contains("hủy giao dịch");
        assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.FAILED);

        verify(paymentRepository).save(order.getPayment());
        verify(orderRepository, never()).save(order);
    }

    @Test
    void processReturnRejectsInvalidChecksum() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "UTE-ORD-12345");
        params.put("vnp_Amount", "50000000");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_SecureHash", "INVALID_HASH_VALUE");

        VNPayCallbackResult result = vnPayService.processReturn(params);

        assertThat(result.success()).isFalse();
        assertThat(result.responseCode()).isEqualTo("97");
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void processIpnConfirmsPaymentSuccessfully() {
        Order order = createOrder("UTE-ORD-99999", new BigDecimal("1000000.00"));
        when(orderRepository.findByOrderCode("UTE-ORD-99999")).thenReturn(Optional.of(order));

        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "UTE-ORD-99999");
        params.put("vnp_Amount", "100000000");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionNo", "14890002");

        String hash = VNPayUtil.hashAllFields(params, HASH_SECRET);
        params.put("vnp_SecureHash", hash);

        VNPayIpnResponse ipnResponse = vnPayService.processIpn(params);

        assertThat(ipnResponse.rspCode()).isEqualTo("00");
        assertThat(ipnResponse.message()).isEqualTo("Confirm Success");
        assertThat(order.getPayment().getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void processIpnRejectsInvalidAmount() {
        Order order = createOrder("UTE-ORD-99999", new BigDecimal("1000000.00"));
        when(orderRepository.findByOrderCode("UTE-ORD-99999")).thenReturn(Optional.of(order));

        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "UTE-ORD-99999");
        params.put("vnp_Amount", "50000000"); // 500k thay vì 1 triệu
        params.put("vnp_ResponseCode", "00");

        String hash = VNPayUtil.hashAllFields(params, HASH_SECRET);
        params.put("vnp_SecureHash", hash);

        VNPayIpnResponse ipnResponse = vnPayService.processIpn(params);

        assertThat(ipnResponse.rspCode()).isEqualTo("04");
        assertThat(ipnResponse.message()).isEqualTo("Invalid amount");
    }

    private Order createOrder(String orderCode, BigDecimal total) {
        User user = new User("Nguyen Van A", "nguyenvana@example.com", "0900000000", "encoded_pwd");
        ReflectionTestUtils.setField(user, "id", 10L);

        Address address = new Address(user, "Nguyen Van A", "0900000000", "01 Vo Van Ngan", "Linh Chieu", "Thu Duc", "TP.HCM", "Nha");
        ReflectionTestUtils.setField(address, "id", 5L);

        Store store = new Store("UTEGear Thủ Đức", "01 Võ Văn Ngân", "TP.HCM", "Thủ Đức",
                "0900000001", null, null, LocalTime.of(8, 0), LocalTime.of(21, 0), null,
                null, null, true);
        ReflectionTestUtils.setField(store, "id", 1L);

        Order order = new Order(orderCode, user, store, null, address, ShippingMethod.HOME_DELIVERY, "Ghi chu");
        order.applyPricing(BigDecimal.ZERO, BigDecimal.ZERO);
        ReflectionTestUtils.setField(order, "id", 100L);
        ReflectionTestUtils.setField(order, "total", total);

        Payment payment = order.createPayment(PaymentMethod.VNPAY);
        ReflectionTestUtils.setField(payment, "amount", total);

        return order;
    }
}

