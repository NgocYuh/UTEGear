package com.utegear.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.utegear.dto.payment.VNPayIpnResponse;
import com.utegear.entity.Address;
import com.utegear.entity.Order;
import com.utegear.entity.PaymentMethod;
import com.utegear.entity.ShippingMethod;
import com.utegear.entity.Store;
import com.utegear.entity.User;
import com.utegear.exception.GlobalExceptionHandler;
import com.utegear.repository.OrderRepository;
import com.utegear.security.CustomUserDetails;
import com.utegear.service.VNPayService;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VNPayService vnPayService;

    @Mock
    private OrderRepository orderRepository;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User("Nguyen Van A", "nguyenvana@example.com", "0900000000", "hash");
        ReflectionTestUtils.setField(currentUser, "id", 10L);

        HandlerMethodArgumentResolver principalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                    NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return new CustomUserDetails(currentUser);
            }
        };

        PaymentController controller = new PaymentController(vnPayService, orderRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(principalResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createPaymentUrlReturnsSuccessForValidOrder() throws Exception {
        Order order = createOrder("UTE-ORD-101", new BigDecimal("750000.00"), currentUser.getId());
        when(orderRepository.findById(101L)).thenReturn(Optional.of(order));
        when(vnPayService.createPaymentUrl(eq(order), any())).thenReturn("https://sandbox.vnpayment.vn/pay-url-demo");

        mockMvc.perform(post("/api/payments/vnpay/create-url").param("orderId", "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentUrl").value("https://sandbox.vnpayment.vn/pay-url-demo"))
                .andExpect(jsonPath("$.orderCode").value("UTE-ORD-101"))
                .andExpect(jsonPath("$.amount").value(750000.00));
    }

    @Test
    void createPaymentUrlRejectsWhenOrderBelongsToDifferentUser() throws Exception {
        Order order = createOrder("UTE-ORD-101", new BigDecimal("750000.00"), 999L); // Khác user
        when(orderRepository.findById(101L)).thenReturn(Optional.of(order));

        mockMvc.perform(post("/api/payments/vnpay/create-url").param("orderId", "101"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void processIpnReturnsVNPayIpnResponse() throws Exception {
        when(vnPayService.processIpn(any())).thenReturn(VNPayIpnResponse.success());

        mockMvc.perform(get("/api/payments/vnpay/ipn")
                        .param("vnp_TxnRef", "UTE-ORD-101")
                        .param("vnp_Amount", "75000000")
                        .param("vnp_ResponseCode", "00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("00"))
                .andExpect(jsonPath("$.Message").value("Confirm Success"));
    }

    private Order createOrder(String orderCode, BigDecimal total, Long userId) {
        User user = new User("User", "user@test.com", null, "hash");
        ReflectionTestUtils.setField(user, "id", userId);

        Address address = new Address(user, "User", "0900000000", "01 Vo Van Ngan", "Linh Chieu", "Thu Duc", "TP.HCM", "Nha");
        ReflectionTestUtils.setField(address, "id", 15L);

        Store store = new Store("UTEGear Thủ Đức", "01 Võ Văn Ngân", "TP.HCM", "Thủ Đức",
                "0900000001", null, null, LocalTime.of(8, 0), LocalTime.of(21, 0), null,
                null, null, true);
        Order order = new Order(orderCode, user, store, null, address, ShippingMethod.HOME_DELIVERY, "Note");
        ReflectionTestUtils.setField(order, "id", 101L);
        ReflectionTestUtils.setField(order, "total", total);
        order.createPayment(PaymentMethod.VNPAY);
        return order;
    }
}

