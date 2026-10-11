package com.utegear.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.utegear.exception.GlobalExceptionHandler;
import com.utegear.repository.BrandRepository;
import com.utegear.repository.CategoryRepository;
import com.utegear.repository.ProductRepository;
import com.utegear.service.BrandService;
import com.utegear.service.CategoryService;
import com.utegear.service.ProductCatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class CatalogControllerTest {
    private ProductRepository products;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        products = mock(ProductRepository.class);
        CatalogController controller = new CatalogController(
                new CategoryService(mock(CategoryRepository.class)),
                new BrandService(mock(BrandRepository.class)),
                new ProductCatalogService(products));
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @SuppressWarnings("unchecked")
    void returnsEmptyProductPageWithPagination() throws Exception {
        when(products.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.empty").value(true));
    }

    @Test
    void rejectsInvalidPriceRange() throws Exception {
        mvc.perform(get("/api/products?minPrice=100&maxPrice=10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FILTER"));
    }

    @Test
    void returns404ForMissingProduct() throws Exception {
        mvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }
}
