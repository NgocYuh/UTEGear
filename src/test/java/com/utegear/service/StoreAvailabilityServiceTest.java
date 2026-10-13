package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.utegear.entity.Category;
import com.utegear.entity.Product;
import com.utegear.entity.ProductStatus;
import com.utegear.entity.ProductVariant;
import com.utegear.entity.Store;
import com.utegear.entity.StoreInventory;
import com.utegear.exception.ApiException;
import com.utegear.repository.ProductRepository;
import com.utegear.repository.ProductVariantRepository;
import com.utegear.repository.StoreInventoryRepository;
import com.utegear.repository.StoreRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class StoreAvailabilityServiceTest {

    private StoreRepository storeRepository;
    private ProductVariantRepository variantRepository;
    private ProductRepository productRepository;
    private StoreInventoryRepository inventoryRepository;
    private StoreAvailabilityService service;

    @BeforeEach
    void setUp() {
        storeRepository = mock(StoreRepository.class);
        variantRepository = mock(ProductVariantRepository.class);
        productRepository = mock(ProductRepository.class);
        inventoryRepository = mock(StoreInventoryRepository.class);
        service = new StoreAvailabilityService(
                storeRepository, variantRepository, productRepository, inventoryRepository);
    }

    @Test
    void returnsZeroWhenStoreHasNoInventoryRow() {
        Store store = store(true);
        ProductVariant variant = product().getVariants().getFirst();
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
        when(variantRepository.findById(31L)).thenReturn(Optional.of(variant));
        when(inventoryRepository.findByStoreIdAndVariantId(1L, 31L)).thenReturn(Optional.empty());

        var result = service.variantAvailability(1L, 31L);

        assertThat(result.quantity()).isZero();
        assertThat(result.available()).isFalse();
    }

    @Test
    void rejectsInactiveStore() {
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store(false)));

        assertThatThrownBy(() -> service.variantAvailability(1L, 31L))
                .isInstanceOf(ApiException.class).extracting("code").isEqualTo("STORE_INACTIVE");
    }

    @Test
    void aggregatesProductStockByActiveVariant() {
        Product product = product();
        ProductVariant variant = product.getVariants().getFirst();
        StoreInventory inventory = new StoreInventory(store(true), variant);
        inventory.receive(5);
        when(productRepository.findByIdAndStatus(21L, ProductStatus.ACTIVE)).thenReturn(Optional.of(product));
        when(inventoryRepository.findProductAvailability(21L, "TP.HCM")).thenReturn(List.of(inventory));

        var result = service.productAvailability(21L, " TP.HCM ");

        assertThat(result.totalStock()).isEqualTo(5);
        assertThat(result.variants()).singleElement().satisfies(stock -> {
            assertThat(stock.variantId()).isEqualTo(31L);
            assertThat(stock.availableStores()).hasSize(1);
        });
    }

    @Test
    void rejectsMissingProduct() {
        when(productRepository.findByIdAndStatus(21L, ProductStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.productAvailability(21L, null))
                .isInstanceOf(ApiException.class).extracting("code").isEqualTo("PRODUCT_NOT_FOUND");
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

    private Store store(boolean active) {
        Store store = new Store("UTEGear", "01 Võ Văn Ngân", "TP.HCM", null, null, null, null,
                LocalTime.of(8, 0), LocalTime.of(21, 0), null, null, null, active);
        ReflectionTestUtils.setField(store, "id", 1L);
        return store;
    }
}
