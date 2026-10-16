package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.entity.Category;
import com.utegear.entity.Product;
import com.utegear.entity.ProductStatus;
import com.utegear.entity.ProductVariant;
import com.utegear.exception.ApiException;
import com.utegear.repository.ProductRepository;
import com.utegear.repository.StoreInventoryRepository;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ProductCatalogServiceTest {

    private ProductRepository productRepository;
    private StoreInventoryRepository inventoryRepository;
    private ProductCatalogService service;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        inventoryRepository = mock(StoreInventoryRepository.class);
        service = new ProductCatalogService(productRepository, inventoryRepository);
    }

    @Test
    void returnsActiveProductDetailWithDefaultVariant() {
        Product product = product();
        when(productRepository.findByIdAndStatus(21L, ProductStatus.ACTIVE)).thenReturn(Optional.of(product));
        when(inventoryRepository.sumStockByVariantIds(any())).thenReturn(java.util.List.of());

        var result = service.detail(21L);

        assertThat(result.id()).isEqualTo(21L);
        assertThat(result.defaultVariantId()).isEqualTo(31L);
        assertThat(result.inStock()).isFalse();
    }

    @Test
    void hidesMissingOrInactiveProduct() {
        when(productRepository.findByIdAndStatus(21L, ProductStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detail(21L))
                .isInstanceOf(ApiException.class).extracting("code").isEqualTo("PRODUCT_NOT_FOUND");
    }

    @Test
    void rejectsInvalidPriceRangeBeforeRepositoryQuery() {
        assertThatThrownBy(() -> service.search(null, null, null,
                new BigDecimal("100"), new BigDecimal("50"), 0, 20, "createdAt,desc"))
                .isInstanceOf(ApiException.class).extracting("code").isEqualTo("INVALID_FILTER");
        verify(productRepository, never()).findAll(
                org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Product>>any(),
                any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void rejectsUnsupportedSort() {
        assertThatThrownBy(() -> service.search(null, null, null, null, null, 0, 20, "soldCount,desc"))
                .isInstanceOf(ApiException.class).extracting("code").isEqualTo("INVALID_FILTER");
    }

    private Product product() {
        Product product = new Product("UTE Mouse", "ute-mouse",
                new Category("Mouse", "mouse", null, 1), null, new BigDecimal("790000"), null);
        ProductVariant variant = new ProductVariant("UTE-MOUSE", Map.of(), true, BigDecimal.ZERO);
        product.addVariant(variant);
        ReflectionTestUtils.setField(product, "id", 21L);
        ReflectionTestUtils.setField(variant, "id", 31L);
        return product;
    }

    @Test
    void showroomUsesOnlyStockFromItsSelectedStore() {
        Product product = product();
        when(productRepository.findInStockAtStore(2L, ProductStatus.ACTIVE)).thenReturn(java.util.List.of(product));
        var stock = mock(StoreInventoryRepository.VariantStock.class);
        when(stock.getVariantId()).thenReturn(31L);
        when(stock.getQuantity()).thenReturn(10L);
        when(inventoryRepository.sumStoreStockByVariantIds(2L, java.util.List.of(31L)))
                .thenReturn(java.util.List.of(stock));
        var result = service.inStockAtStore(2L);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().totalStock()).isEqualTo(10);
        verify(inventoryRepository, never()).sumStockByVariantIds(any());
        verify(productRepository).findWithImagesByIds(java.util.List.of(21L));
    }

    @Test
    void showroomWithNoStockReturnsAnEmptyListWithoutImageOrStockQueries() {
        when(productRepository.findInStockAtStore(3L, ProductStatus.ACTIVE)).thenReturn(java.util.List.of());
        assertThat(service.inStockAtStore(3L)).isEmpty();
        verify(productRepository, never()).findWithImagesByIds(any());
        verify(inventoryRepository, never()).sumStoreStockByVariantIds(any(), any());
    }
}
