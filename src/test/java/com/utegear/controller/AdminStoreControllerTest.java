package com.utegear.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.utegear.exception.GlobalExceptionHandler;
import com.utegear.repository.StoreRepository;
import com.utegear.service.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminStoreControllerTest {

    private MockMvc mvc;
    private StoreService service;

    @BeforeEach
    void setUp() {
        StoreRepository repository = mock(StoreRepository.class);
        service = new StoreService(repository);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        mvc = MockMvcBuilders.standaloneSetup(new AdminStoreController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createsValidStore() throws Exception {
        mvc.perform(post("/api/admin/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("UTEGear Quận 1"));
    }

    @Test
    void rejectsInvalidCoordinatesAndMissingName() throws Exception {
        mvc.perform(post("/api/admin/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "address": "01 Võ Văn Ngân",
                                  "province": "TP.HCM",
                                  "openingTime": "08:00",
                                  "closingTime": "21:00",
                                  "latitude": 91,
                                  "active": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.latitude").exists());
    }

    private String validJson() {
        return """
                {
                  "name": "UTEGear Quận 1",
                  "address": "01 Võ Văn Ngân",
                  "province": "TP.HCM",
                  "district": "Thủ Đức",
                  "phone": "0909000001",
                  "openingTime": "08:00",
                  "closingTime": "21:00",
                  "active": true
                }
                """;
    }
}
