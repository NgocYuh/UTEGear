package com.utegear.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.utegear.dto.notification.NotificationPage;
import com.utegear.dto.order.AdminOrderDtos;
import com.utegear.dto.order.CustomerOrderDtos;
import com.utegear.dto.order.CheckoutQuoteResponse;
import com.utegear.entity.Role;
import com.utegear.entity.User;
import com.utegear.repository.UserRepository;
import com.utegear.service.AdminOrderService;
import com.utegear.service.AddressService;
import com.utegear.service.CheckoutService;
import com.utegear.service.CustomerOrderService;
import com.utegear.service.NotificationService;
import com.utegear.service.ProductMediaService;
import com.utegear.service.ProfileService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProtectedApiFlowTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @MockBean private UserRepository userRepository;
    @MockBean private CheckoutService checkoutService;
    @MockBean private NotificationService notificationService;
    @MockBean private AdminOrderService adminOrderService;
    @MockBean private ProductMediaService productMediaService;
    @MockBean private ProfileService profileService;
    @MockBean private AddressService addressService;
    @MockBean private CustomerOrderService customerOrderService;

    private User customer;
    private User admin;
    private String customerToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        customer = user(7L, "customer@example.test", Role.CUSTOMER);
        admin = user(8L, "admin@example.test", Role.ADMIN);
        customerToken = jwtService.generateToken(new CustomUserDetails(customer));
        adminToken = jwtService.generateToken(new CustomUserDetails(admin));
        when(userRepository.findByEmailIgnoreCase(customer.getEmail())).thenReturn(Optional.of(customer));
        when(userRepository.findByEmailIgnoreCase(admin.getEmail())).thenReturn(Optional.of(admin));
    }

    @Test
    void rejectsCheckoutAndNotificationsWithoutJwt() throws Exception {
        mockMvc.perform(post("/api/checkout/quote").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void rejectsAccountAndCustomerOrderApisWithoutJwt() throws Exception {
        mockMvc.perform(patch("/api/profile").contentType("application/json")
                        .content("{\"fullName\":\"Test User\",\"phone\":\"0900000000\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void accountAndCustomerOrderApisUseUserFromJwt() throws Exception {
        when(profileService.update(eq(customer), any())).thenReturn(new com.utegear.dto.auth.UserProfile(
                customer.getId(), "Updated User", customer.getEmail(), "0900000000", null,
                true, List.of(Role.CUSTOMER), customer.getCreatedAt()));
        when(addressService.findAll(customer)).thenReturn(List.of());
        when(customerOrderService.list(customer, null, 0, 20)).thenReturn(
                new CustomerOrderDtos.OrderPage(List.of(), 0, 20, 0, 0, true, true, true));

        mockMvc.perform(patch("/api/profile")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType("application/json")
                        .content("{\"fullName\":\"Updated User\",\"phone\":\"0900000000\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Updated User"));
        mockMvc.perform(get("/api/addresses").header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
        mockMvc.perform(get("/api/orders").header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.empty").value(true));

        verify(profileService).update(eq(customer), any());
        verify(addressService).findAll(customer);
        verify(customerOrderService).list(customer, null, 0, 20);
    }

    @Test
    void checkoutUsesUserFromJwt() throws Exception {
        var response = new CheckoutQuoteResponse("quote", OffsetDateTime.now(), List.of(),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                null, new CheckoutQuoteResponse.StoreRef(2L, "UTEGear"), List.of());
        when(checkoutService.quote(eq(customer), any())).thenReturn(response);

        mockMvc.perform(post("/api/checkout/quote")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType("application/json")
                        .content("""
                                {"addressId":1,"fulfillmentStoreId":2,"paymentMethod":"COD",
                                 "items":[{"variantId":10,"quantity":1}]}
                                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.quoteToken").value("quote"));
        verify(checkoutService).quote(eq(customer), any());
    }

    @Test
    void notificationQueryUsesUserFromJwt() throws Exception {
        when(notificationService.list(customer, false, 0, 20))
                .thenReturn(new NotificationPage(List.of(), 0, 20, 0, 0, true, true, true));

        mockMvc.perform(get("/api/notifications?read=false")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.empty").value(true));
        verify(notificationService).list(customer, false, 0, 20);
    }

    @Test
    void customerCannotAccessAdminOrderOrMediaApis() throws Exception {
        mockMvc.perform(get("/api/admin/orders").header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
        mockMvc.perform(delete("/api/admin/products/21/images/3")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void adminCanReachAdminOrderController() throws Exception {
        when(adminOrderService.list(null, null, null, 0, 20))
                .thenReturn(new AdminOrderDtos.OrderPage(List.of(), 0, 20, 0, 0));

        mockMvc.perform(get("/api/admin/orders").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray());
        verify(adminOrderService).list(null, null, null, 0, 20);
    }

    @Test
    void invalidAdminStatusRequestReturnsValidationError() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/admin/orders/10/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    private User user(Long id, String email, String role) {
        User user = new User("Test User", email, null, "encoded-password");
        user.getRoles().add(new Role(role));
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
