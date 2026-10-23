package com.utegear.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.utegear.dto.payment.VNPayCallbackResult;
import com.utegear.service.VNPayService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PaymentViewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VNPayService vnPayService;

    @BeforeEach
    void setUp() {
        PaymentViewController controller = new PaymentViewController(vnPayService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void vnpayReturnRendersPaymentReturnTemplateWithResult() throws Exception {
        VNPayCallbackResult callbackResult = new VNPayCallbackResult(
                true, "UTE-ORD-101", "1490001", new BigDecimal("500000.00"), "NCB", "00", "Giao dịch thành công");
        when(vnPayService.processReturn(any())).thenReturn(callbackResult);
        when(vnPayService.getResponseMessage("00")).thenReturn("Giao dịch thành công");

        mockMvc.perform(get("/payment/vnpay-return")
                        .param("vnp_TxnRef", "UTE-ORD-101")
                        .param("vnp_ResponseCode", "00"))
                .andExpect(status().isOk())
                .andExpect(view().name("payment/vnpay-return"))
                .andExpect(model().attributeExists("result"))
                .andExpect(model().attributeExists("responseMessage"));
    }
}

