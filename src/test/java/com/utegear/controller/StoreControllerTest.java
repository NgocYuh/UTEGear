package com.utegear.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.utegear.entity.Category;
import com.utegear.entity.Product;
import com.utegear.entity.ProductStatus;
import com.utegear.entity.ProductVariant;
import com.utegear.entity.Store;
import com.utegear.entity.StoreInventory;
import com.utegear.exception.GlobalExceptionHandler;
import com.utegear.repository.ProductRepository;
import com.utegear.repository.ProductVariantRepository;
import com.utegear.repository.StoreInventoryRepository;
import com.utegear.repository.StoreRepository;
import com.utegear.service.StoreAvailabilityService;
import com.utegear.service.StoreService;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class StoreControllerTest {

    private StoreRepository stores;
    private ProductVariantRepository variants;
    private ProductRepository products;
    private StoreInventoryRepository inventory;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        stores = mock(StoreRepository.class);
        variants = mock(ProductVariantRepository.class);
        products = mock(ProductRepository.class);
        inventory = mock(StoreInventoryRepository.class);
        StoreController controller = new StoreController(
                new StoreService(stores),
                new StoreAvailabilityService(stores, variants, products, inventory));
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listsActiveStoresByProvince() throws Exception {
        Store store = store(11L, "UTEGear Thủ Đức", true);
        when(stores.findByActiveAndProvinceIgnoreCaseOrderByNameAsc(true, "TP. Hồ Chí Minh"))
                .thenReturn(List.of(store));

        mvc.perform(get("/api/stores")
                        .param("province", " TP. Hồ Chí Minh "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].name").value("UTEGear Thủ Đức"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void hidesInactiveStoreDetail() throws Exception {
        when(stores.findByIdAndActiveTrue(12L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/stores/12"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("STORE_NOT_FOUND"));
    }

    @Test
    void returnsVariantInStockAtStore() throws Exception {
        Store store = store(11L, "UTEGear Thủ Đức", true);
        ProductVariant variant = variant(1001L, "M1-DEFAULT", true);
        StoreInventory stock = new StoreInventory(store, variant);
        stock.receive(8);
        when(stores.findById(11L)).thenReturn(Optional.of(store));
        when(variants.findById(1001L)).thenReturn(Optional.of(variant));
        when(inventory.findByStoreIdAndVariantId(11L, 1001L)).thenReturn(Optional.of(stock));

        mvc.perform(get("/api/stores/11/availability").param("variantId", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(8))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.sku").value("M1-DEFAULT"));
    }

    @Test
    void requiresVariantIdForStoreAvailability() throws Exception {
        mvc.perform(get("/api/stores/11/availability"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.variantId").exists());
    }

    @Test
    void returnsOutOfStockWhenInventoryRowDoesNotExist() throws Exception {
        Store store = store(11L, "UTEGear Thủ Đức", true);
        ProductVariant variant = variant(1003L, "K87-WHITE-BROWN", false);
        when(stores.findById(11L)).thenReturn(Optional.of(store));
        when(variants.findById(1003L)).thenReturn(Optional.of(variant));
        when(inventory.findByStoreIdAndVariantId(11L, 1003L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/stores/11/availability").param("variantId", "1003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(0))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void rejectsAvailabilityForInactiveStore() throws Exception {
        when(stores.findById(12L)).thenReturn(Optional.of(store(12L, "UTEGear Tạm đóng", false)));

        mvc.perform(get("/api/stores/12/availability").param("variantId", "1001"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STORE_INACTIVE"));
    }

    @Test
    void aggregatesProductAvailabilityAcrossVariantsAndStores() throws Exception {
        Product product = product(102L);
        ProductVariant defaultVariant = variant(1001L, "K87-DEFAULT", true);
        ProductVariant selectedVariant = variant(1002L, "K87-BLACK-RED", false);
        product.addVariant(defaultVariant);
        product.addVariant(selectedVariant);
        Store firstStore = store(11L, "UTEGear Thủ Đức", true);
        Store secondStore = store(13L, "UTEGear Quận 1", true);
        StoreInventory firstStock = stock(firstStore, defaultVariant, 3);
        StoreInventory secondStock = stock(secondStore, defaultVariant, 2);
        StoreInventory selectedStock = stock(firstStore, selectedVariant, 4);
        when(products.findByIdAndStatus(102L, ProductStatus.ACTIVE)).thenReturn(Optional.of(product));
        when(inventory.findProductAvailability(102L, "TP. Hồ Chí Minh"))
                .thenReturn(List.of(firstStock, secondStock, selectedStock));

        mvc.perform(get("/api/products/102/availability")
                        .param("province", "TP. Hồ Chí Minh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStock").value(9))
                .andExpect(jsonPath("$.variants[0].totalStock").value(5))
                .andExpect(jsonPath("$.variants[0].availableStores.length()").value(2))
                .andExpect(jsonPath("$.variants[1].totalStock").value(4));
        verify(inventory).findProductAvailability(102L, "TP. Hồ Chí Minh");
    }

    private StoreInventory stock(Store store, ProductVariant variant, int quantity) {
        StoreInventory stock = new StoreInventory(store, variant);
        stock.receive(quantity);
        return stock;
    }

    private Store store(Long id, String name, boolean active) {
        Store store = new Store(name, "01 Võ Văn Ngân", "TP. Hồ Chí Minh", "Thủ Đức",
                null, null, null, LocalTime.of(8, 0), LocalTime.of(21, 0),
                null, null, null, active);
        ReflectionTestUtils.setField(store, "id", id);
        return store;
    }

    private ProductVariant variant(Long id, String sku, boolean defaultVariant) {
        ProductVariant variant = new ProductVariant(sku, Map.of(), defaultVariant, BigDecimal.ZERO);
        ReflectionTestUtils.setField(variant, "id", id);
        return variant;
    }

    private Product product(Long id) {
        Product product = new Product("Bàn phím K87", "ban-phim-k87",
                new Category("Bàn phím", "ban-phim", null, 1), null,
                BigDecimal.valueOf(900_000), null);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
