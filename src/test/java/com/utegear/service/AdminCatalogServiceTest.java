package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.utegear.dto.admin.AdminDtos;
import com.utegear.entity.ProductStatus;
import com.utegear.exception.ApiException;
import com.utegear.repository.BrandRepository;
import com.utegear.repository.CategoryRepository;
import com.utegear.repository.ProductRepository;
import com.utegear.repository.ProductVariantRepository;
import com.utegear.repository.StoreInventoryRepository;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminCatalogServiceTest {

    private AdminCatalogService service;

    @BeforeEach
    void setUp() {
        service = new AdminCatalogService(
                mock(CategoryRepository.class), mock(BrandRepository.class),
                mock(ProductRepository.class), mock(ProductVariantRepository.class),
                mock(StoreInventoryRepository.class));
    }

    @Test
    void rejectsSalePriceAboveBasePriceAsClientError() {
        AdminDtos.ProductRequest request = request(
                new BigDecimal("100000"), new BigDecimal("120000"), JsonNodeFactory.instance.objectNode());

        assertThatThrownBy(() -> service.createProduct(request))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void rejectsNonObjectSpecificationsAsClientError() {
        AdminDtos.ProductRequest request = request(
                new BigDecimal("100000"), null, JsonNodeFactory.instance.arrayNode());

        assertThatThrownBy(() -> service.createProduct(request))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void productsValidatesPaginationBounds() {
        assertThatThrownBy(() -> service.products(null, null, -1, 20))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");

        assertThatThrownBy(() -> service.products(null, null, 0, 0))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");

        assertThatThrownBy(() -> service.products(null, null, 0, 101))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");
    }

    private AdminDtos.ProductRequest request(
            BigDecimal basePrice, BigDecimal salePrice, com.fasterxml.jackson.databind.JsonNode specifications) {
        return new AdminDtos.ProductRequest(
                "Chuột UTEGear", 1L, null, null, basePrice, salePrice,
                ProductStatus.ACTIVE, "MOUSE-DEFAULT", Map.of(), specifications);
    }
}
