package com.utegear.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.utegear.dto.wishlist.WishlistItemResponse;
import com.utegear.entity.Role;
import com.utegear.entity.User;
import com.utegear.repository.UserRepository;
import com.utegear.security.CustomUserDetails;
import com.utegear.security.JwtService;
import com.utegear.service.WishlistService;
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
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private WishlistService wishlistService;

    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        user = new User("Wishlist User", "wishlist@example.test", null, "encoded-password");
        user.getRoles().add(new Role(Role.CUSTOMER));
        ReflectionTestUtils.setField(user, "id", 7L);
        token = jwtService.generateToken(new CustomUserDetails(user));
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void rejectsWishlistWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/wishlist"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void listsCurrentUsersWishlist() throws Exception {
        when(wishlistService.list(user)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/wishlist")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].product.id").value(21))
                .andExpect(jsonPath("$[0].product.defaultVariantId").value(31));

        verify(wishlistService).list(user);
    }

    @Test
    void addsProductToCurrentUsersWishlist() throws Exception {
        when(wishlistService.add(eq(user), eq(21L))).thenReturn(response());

        mockMvc.perform(post("/api/wishlist/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {"productId": 21}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.product.id").value(21));

        verify(wishlistService).add(user, 21L);
    }

    @Test
    void rejectsInvalidProductId() throws Exception {
        mockMvc.perform(post("/api/wishlist/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {"productId": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.productId").exists());
    }

    @Test
    void removesProductFromCurrentUsersWishlist() throws Exception {
        mockMvc.perform(delete("/api/wishlist/items/21")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        verify(wishlistService).remove(user, 21L);
    }

    private WishlistItemResponse response() {
        var product = new WishlistItemResponse.ProductSummary(
                21L, "UTE Mouse", "ute-mouse", new BigDecimal("790000"), null, 31L, true);
        return new WishlistItemResponse(product, OffsetDateTime.parse("2026-10-13T01:00:00Z"));
    }
}
