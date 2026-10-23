package com.utegear.security;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.utegear.entity.Role;
import com.utegear.entity.Store;
import com.utegear.entity.User;
import com.utegear.repository.UserRepository;
import com.utegear.repository.StoreRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SecurityIntegrationTest.AdminTestController.class)
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private StoreRepository storeRepository;

    @MockBean
    private com.utegear.service.TokenBlacklistService tokenBlacklistService;

    @Test
    void permitsPublicAuthEndpointWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void rejectsProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void rejectsProtectedEndpointWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void rejectsProtectedEndpointWithExpiredToken() throws Exception {
        CustomUserDetails principal = principal(Role.CUSTOMER);
        JwtService shortLivedJwtService = new JwtService(
                "test-only-secret-that-must-never-be-used-outside-tests", 1);
        String token = shortLivedJwtService.generateToken(principal);
        Thread.sleep(20);

        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void permitsPublicCatalogPathWithoutToken() throws Exception {
        mockMvc.perform(get("/api/stores"))
                .andExpect(status().isOk());
    }

    @Test
    void acceptsValidTokenForProfile() throws Exception {
        CustomUserDetails principal = principal(Role.CUSTOMER);
        String token = jwtService.generateToken(principal);
        when(userRepository.findByEmailIgnoreCase(principal.getUsername()))
                .thenReturn(Optional.of(principal.getUser()));
        when(userRepository.findById(principal.getUser().getId()))
                .thenReturn(Optional.of(principal.getUser()));

        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.roles[0]").value(Role.CUSTOMER));
    }

    @Test
    void blocksCustomerFromAdminEndpoint() throws Exception {
        CustomUserDetails principal = principal(Role.CUSTOMER);
        String token = jwtService.generateToken(principal);
        when(userRepository.findByEmailIgnoreCase(principal.getUsername()))
                .thenReturn(Optional.of(principal.getUser()));

        mockMvc.perform(get("/api/admin/security-test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void blocksCustomerFromAdminOrderManagement() throws Exception {
        CustomUserDetails principal = principal(Role.CUSTOMER);
        String token = jwtService.generateToken(principal);
        when(userRepository.findByEmailIgnoreCase(principal.getUsername()))
                .thenReturn(Optional.of(principal.getUser()));

        mockMvc.perform(get("/api/admin/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void permitsAdminOnAdminEndpoint() throws Exception {
        CustomUserDetails principal = principal(Role.ADMIN);
        String token = jwtService.generateToken(principal);
        when(userRepository.findByEmailIgnoreCase(principal.getUsername()))
                .thenReturn(Optional.of(principal.getUser()));

        mockMvc.perform(get("/api/admin/security-test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void blocksCustomerFromStoreManagement() throws Exception {
        CustomUserDetails principal = principal(Role.CUSTOMER);
        String token = jwtService.generateToken(principal);
        when(userRepository.findByEmailIgnoreCase(principal.getUsername()))
                .thenReturn(Optional.of(principal.getUser()));

        mockMvc.perform(post("/api/admin/stores")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(validStoreJson()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void permitsAdminToCreateStore() throws Exception {
        CustomUserDetails principal = principal(Role.ADMIN);
        String token = jwtService.generateToken(principal);
        when(userRepository.findByEmailIgnoreCase(principal.getUsername()))
                .thenReturn(Optional.of(principal.getUser()));
        when(storeRepository.save(any(Store.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/admin/stores")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(validStoreJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("UTEGear Quận 1"));
    }

    @Test
    void blocksCustomerFromProductImport() throws Exception {
        CustomUserDetails principal = principal(Role.CUSTOMER);
        String token = jwtService.generateToken(principal);
        when(userRepository.findByEmailIgnoreCase(principal.getUsername()))
                .thenReturn(Optional.of(principal.getUser()));
        MockMultipartFile file = new MockMultipartFile(
                "file", "products.jsonl", "application/x-ndjson", "{}".getBytes());

        mockMvc.perform(multipart("/api/admin/products/import")
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    private String validStoreJson() {
        return """
                {
                  "name": "UTEGear Quận 1",
                  "address": "01 Võ Văn Ngân",
                  "province": "TP.HCM",
                  "openingTime": "08:00",
                  "closingTime": "21:00",
                  "active": true
                }
                """;
    }

    private CustomUserDetails principal(String roleName) {
        User user = new User("Test User", "user@example.com", null, "encoded-password");
        user.getRoles().add(new Role(roleName));
        ReflectionTestUtils.setField(user, "id", 1L);
        return new CustomUserDetails(user);
    }

    @RestController
    static class AdminTestController {

        @GetMapping("/api/admin/security-test")
        StatusResponse status() {
            return new StatusResponse("ok");
        }
    }

    record StatusResponse(String status) {
    }
}
