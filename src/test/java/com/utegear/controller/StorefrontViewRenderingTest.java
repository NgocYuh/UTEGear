package com.utegear.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.utegear.dto.catalog.ProductDtos;
import com.utegear.service.BrandService;
import com.utegear.service.CategoryService;
import com.utegear.service.ProductCatalogService;
import com.utegear.service.StoreService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StorefrontViewRenderingTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private CategoryService categoryService;
    @MockBean private BrandService brandService;
    @MockBean private ProductCatalogService productCatalogService;
    @MockBean private StoreService storeService;

    @BeforeEach
    void setUp() {
        ProductDtos.ProductPage emptyProducts = new ProductDtos.ProductPage(
                List.of(), 0, 20, 0, 0, true, true, true);
        when(categoryService.active()).thenReturn(List.of());
        when(brandService.active()).thenReturn(List.of());
        when(storeService.findPublic(true, null)).thenReturn(List.of());
        when(productCatalogService.search(
                any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString()))
                .thenReturn(emptyProducts);
        when(productCatalogService.search(
                isNull(), isNull(), isNull(), isNull(), isNull(), anyInt(), anyInt(), anyString()))
                .thenReturn(emptyProducts);
    }

    @Test
    void rendersPublicAndAuthenticationPagesWithSharedLayout() throws Exception {
        for (String path : List.of("/", "/products", "/search?q=mouse", "/stores",
                "/login", "/register", "/about", "/contact")) {
            mockMvc.perform(get(path))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("text/html"));
        }
    }

    @Test
    void rendersCustomerShellPagesWithoutServerSideJwt() throws Exception {
        for (String path : List.of("/cart", "/wishlist", "/checkout", "/profile")) {
            mockMvc.perform(get(path))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("text/html"));
        }
    }
}
