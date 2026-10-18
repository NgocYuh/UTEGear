package com.utegear.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.utegear.dto.consultant.ConsultantDtos.*;
import com.utegear.service.GearConsultantService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GearConsultantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GearConsultantService service;

    @Test
    void getQuickPrompts_returnsOk() throws Exception {
        when(service.getQuickPrompts()).thenReturn(List.of(
                new QuickPrompt("p1", "Prompt 1", "Message 1", "bi-star")));

        mockMvc.perform(get("/api/consultant/quick-prompts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("p1"))
                .andExpect(jsonPath("$[0].title").value("Prompt 1"));
    }

    @Test
    void chat_withValidRequest_returnsResponse() throws Exception {
        ChatResponse mockResponse = new ChatResponse(
                "Tư vấn thành công",
                "FPS Gaming",
                new StoreInfo(3L, "UTEGear Demo Đà Nẵng", "Đà Nẵng", "Hải Châu"),
                new BigDecimal("1500000"),
                new BigDecimal("1298000"),
                List.of(),
                List.of("Gợi ý tiếp theo"));

        when(service.consult(any())).thenReturn(mockResponse);

        ChatRequest request = new ChatRequest("Tư vấn chuột FPS dưới 1.5 triệu ở Đà Nẵng", 3L);

        mockMvc.perform(post("/api/consultant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Tư vấn thành công"))
                .andExpect(jsonPath("$.store.name").value("UTEGear Demo Đà Nẵng"));
    }

    @Test
    void chat_withBlankMessage_returnsBadRequest() throws Exception {
        ChatRequest request = new ChatRequest("   ", 1L);

        mockMvc.perform(post("/api/consultant/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }
}

