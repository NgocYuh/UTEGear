package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.dto.inventory.InventoryResult;
import com.utegear.dto.inventory.InventoryTransferResult;
import com.utegear.entity.Address;
import com.utegear.entity.Category;
import com.utegear.entity.Order;
import com.utegear.entity.ProductVariant;
import com.utegear.entity.Product;
import com.utegear.entity.ShippingMethod;
import com.utegear.entity.Store;
import com.utegear.entity.StoreInventory;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.ProductVariantRepository;
import com.utegear.repository.StoreInventoryRepository;
import com.utegear.repository.StoreRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class InventoryServiceTest {

    private StoreInventoryRepository inventoryRepository;
    private StoreRepository storeRepository;
    private ProductVariantRepository variantRepository;
    private InventoryService service;
    private Store source;
    private Store destination;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(StoreInventoryRepository.class);
        storeRepository = mock(StoreRepository.class);
        variantRepository = mock(ProductVariantRepository.class);
        service = new InventoryService(inventoryRepository, storeRepository, variantRepository);
        source = store("Nguồn", true);
        destination = store("Đích", true);
        variant = new ProductVariant("SKU-01", Map.of(), true, BigDecimal.ZERO);
        when(storeRepository.findLockedById(1L)).thenReturn(Optional.of(source));
        when(storeRepository.findLockedById(2L)).thenReturn(Optional.of(destination));
        when(variantRepository.findById(10L)).thenReturn(Optional.of(variant));
        when(inventoryRepository.save(any(StoreInventory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void receivesInventoryForNewStoreVariantPair() {
        when(inventoryRepository.findLocked(1L, 10L)).thenReturn(Optional.empty());

        InventoryResult result = service.receive(1L, 10L, 12);

        assertThat(result.quantity()).isEqualTo(12);
        assertThat(result.updatedAt()).isNotNull();
        verify(inventoryRepository).save(any(StoreInventory.class));
    }

    @Test
    void addsInventoryToExistingPair() {
        StoreInventory inventory = inventory(source, 7);
        when(inventoryRepository.findLocked(1L, 10L)).thenReturn(Optional.of(inventory));

        InventoryResult result = service.receive(1L, 10L, 5);

        assertThat(result.quantity()).isEqualTo(12);
    }

    @Test
    void decreasesInventoryWithoutGoingBelowZero() {
        StoreInventory inventory = inventory(source, 8);
        when(inventoryRepository.findLocked(1L, 10L)).thenReturn(Optional.of(inventory));

        InventoryResult result = service.decrease(1L, 10L, 3);

        assertThat(result.quantity()).isEqualTo(5);
    }

    @Test
    void transfersInventoryAndPreservesTotal() {
        StoreInventory sourceInventory = inventory(source, 10);
        StoreInventory destinationInventory = inventory(destination, 3);
        when(inventoryRepository.findLocked(1L, 10L)).thenReturn(Optional.of(sourceInventory));
        when(inventoryRepository.findLocked(2L, 10L)).thenReturn(Optional.of(destinationInventory));
        int totalBefore = sourceInventory.getQuantity() + destinationInventory.getQuantity();

        InventoryTransferResult result = service.transfer(1L, 2L, 10L, 4);

        assertThat(result.sourceQuantity()).isEqualTo(6);
        assertThat(result.destinationQuantity()).isEqualTo(7);
        assertThat(result.sourceQuantity() + result.destinationQuantity()).isEqualTo(totalBefore);
    }

    @Test
    void createsDestinationInventoryDuringTransfer() {
        StoreInventory sourceInventory = inventory(source, 10);
        when(inventoryRepository.findLocked(1L, 10L)).thenReturn(Optional.of(sourceInventory));
        when(inventoryRepository.findLocked(2L, 10L)).thenReturn(Optional.empty());

        InventoryTransferResult result = service.transfer(1L, 2L, 10L, 4);

        assertThat(result.sourceQuantity()).isEqualTo(6);
        assertThat(result.destinationQuantity()).isEqualTo(4);
    }

    @Test
    void rejectsTransferWhenSourceHasInsufficientStock() {
        StoreInventory sourceInventory = inventory(source, 2);
        StoreInventory destinationInventory = inventory(destination, 3);
        when(inventoryRepository.findLocked(1L, 10L)).thenReturn(Optional.of(sourceInventory));
        when(inventoryRepository.findLocked(2L, 10L)).thenReturn(Optional.of(destinationInventory));

        assertThatThrownBy(() -> service.transfer(1L, 2L, 10L, 4))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("INSUFFICIENT_STOCK");
        assertThat(sourceInventory.getQuantity()).isEqualTo(2);
        assertThat(destinationInventory.getQuantity()).isEqualTo(3);
        verify(inventoryRepository, never()).save(any(StoreInventory.class));
    }

    @Test
    void rejectsTransferBetweenSameStore() {
        assertThatThrownBy(() -> service.transfer(1L, 1L, 10L, 1))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void rejectsInventoryQuantityOutOfBounds() {
        assertThatThrownBy(() -> service.setQuantity(1L, 10L, -1))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");

        assertThatThrownBy(() -> service.setQuantity(1L, 10L, 1000001))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");

        assertThatThrownBy(() -> service.receive(1L, 10L, 0))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");

        assertThatThrownBy(() -> service.receive(1L, 10L, 1000001))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void setsQuantityWithinValidRange() {
        when(inventoryRepository.findLocked(1L, 10L)).thenReturn(Optional.empty());

        InventoryResult result = service.setQuantity(1L, 10L, 50);

        assertThat(result.quantity()).isEqualTo(50);
        verify(inventoryRepository).save(any(StoreInventory.class));
    }

    @Test
    void rejectsOperationForInactiveStore() {
        when(storeRepository.findLockedById(1L)).thenReturn(Optional.of(store("Tạm đóng", false)));

        assertThatThrownBy(() -> service.receive(1L, 10L, 1))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("STORE_INACTIVE");
    }

    @Test
    void restoresEachReservedOrderLineToItsStoreInventory() {
        ReflectionTestUtils.setField(source, "id", 1L);
        Product product = new Product("Chuột M1", "chuot-m1",
                new Category("Chuột", "chuot", null, 0), null, new BigDecimal("500000"), null);
        product.addVariant(variant);
        ReflectionTestUtils.setField(variant, "id", 10L);
        User user = new User("Nguyễn Văn An", "an@example.test", null, "hash");
        ReflectionTestUtils.setField(user, "id", 7L);
        Address address = new Address(user, "Nguyễn Văn An", "0900000001", "01 Võ Văn Ngân",
                "Linh Chiểu", "Thủ Đức", "TP.HCM", "Nhà");
        Order order = new Order("UTE-RESTORE-1", user, source, null, address,
                ShippingMethod.HOME_DELIVERY, null);
        order.addItem(variant, 2, null);
        StoreInventory inventory = inventory(source, 3);
        when(inventoryRepository.findLocked(1L, 10L)).thenReturn(Optional.of(inventory));

        service.restoreReservedStock(order);

        assertThat(inventory.getQuantity()).isEqualTo(5);
        verify(inventoryRepository).save(inventory);
    }

    private StoreInventory inventory(Store store, int quantity) {
        StoreInventory inventory = new StoreInventory(store, variant);
        inventory.receive(quantity);
        return inventory;
    }

    private Store store(String name, boolean active) {
        return new Store(
                name, "01 Võ Văn Ngân", "TP.HCM", null, null, null, null,
                LocalTime.of(8, 0), LocalTime.of(21, 0), null, null, null, active);
    }
}
