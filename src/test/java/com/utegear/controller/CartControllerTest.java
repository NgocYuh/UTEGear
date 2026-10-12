package com.utegear.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.utegear.dto.cart.CartResponse;
import com.utegear.entity.Role;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.UserRepository;
import com.utegear.security.CustomUserDetails;
import com.utegear.security.JwtService;
import com.utegear.service.CartApiService;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CartApiService cartService;

    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        user = new User("Cart User", "cart@example.test", null, "encoded-password");
        user.getRoles().add(new Role(Role.CUSTOMER));
        ReflectionTestUtils.setField(user, "id", 7L);
        token = jwtService.generateToken(new CustomUserDetails(user));
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void rejectsCartWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void addsItemToAuthenticatedUsersCart() throws Exception {
        when(cartService.add(eq(user), eq(10L), eq(2))).thenReturn(emptyCart(301L));

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {"variantId": 10, "quantity": 2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(301))
                .andExpect(jsonPath("$.items").isArray());

        verify(cartService).add(user, 10L, 2);
    }

    @Test
    void rejectsInvalidQuantity() throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {"variantId": 10, "quantity": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.quantity").exists());
    }

    @Test
    void hidesAnotherUsersCartItem() throws Exception {
        when(cartService.update(eq(user), eq(99L), eq(2))).thenThrow(new ApiException(
                HttpStatus.NOT_FOUND, "CART_ITEM_NOT_FOUND", "Không tìm thấy sản phẩm trong giỏ hàng"));

        mockMvc.perform(put("/api/cart/items/99")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {"quantity": 2}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CART_ITEM_NOT_FOUND"));
    }

    private CartResponse emptyCart(Long id) {
        return new CartResponse(id, List.of(), 0, BigDecimal.ZERO, OffsetDateTime.now());
    }
}
