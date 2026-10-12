package com.utegear.service;

import com.utegear.dto.inventory.InventoryResult;
import com.utegear.dto.inventory.InventoryTransferResult;
import com.utegear.entity.ProductVariant;
import com.utegear.entity.Store;
import com.utegear.entity.StoreInventory;
import com.utegear.exception.ApiException;
import com.utegear.repository.ProductVariantRepository;
import com.utegear.repository.StoreInventoryRepository;
import com.utegear.repository.StoreRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final StoreInventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;
    private final ProductVariantRepository variantRepository;

    public InventoryService(
            StoreInventoryRepository inventoryRepository,
            StoreRepository storeRepository,
            ProductVariantRepository variantRepository) {
        this.inventoryRepository = inventoryRepository;
        this.storeRepository = storeRepository;
        this.variantRepository = variantRepository;
    }

    @Transactional
    public InventoryResult receive(Long storeId, Long variantId, int quantity) {
        requireIds(storeId, variantId);
        requirePositive(quantity);
        Store store = requireActiveStore(storeId);
        ProductVariant variant = requireActiveVariant(variantId);
        StoreInventory inventory = inventoryRepository.findLocked(storeId, variantId)
                .orElseGet(() -> new StoreInventory(store, variant));
        inventory.receive(quantity);
        return InventoryResult.from(inventoryRepository.save(inventory));
    }

    @Transactional
    public InventoryResult decrease(Long storeId, Long variantId, int quantity) {
        requireIds(storeId, variantId);
        requirePositive(quantity);
        requireActiveStore(storeId);
        requireActiveVariant(variantId);
        StoreInventory inventory = requireInventory(storeId, variantId);
        decreaseOrThrow(inventory, quantity);
        return InventoryResult.from(inventoryRepository.save(inventory));
    }

    @Transactional
    public InventoryTransferResult transfer(
            Long sourceStoreId,
            Long destinationStoreId,
            Long variantId,
            int quantity) {
        requireIds(sourceStoreId, destinationStoreId, variantId);
        requirePositive(quantity);
        if (sourceStoreId.equals(destinationStoreId)) {
            throw validation("Cửa hàng nguồn và đích phải khác nhau");
        }

        boolean sourceFirst = sourceStoreId < destinationStoreId;
        Store firstStore = requireActiveStore(sourceFirst ? sourceStoreId : destinationStoreId);
        Store secondStore = requireActiveStore(sourceFirst ? destinationStoreId : sourceStoreId);
        Store source = sourceFirst ? firstStore : secondStore;
        Store destination = sourceFirst ? secondStore : firstStore;
        ProductVariant variant = requireActiveVariant(variantId);

        StoreInventory sourceInventory = requireInventory(sourceStoreId, variantId);
        StoreInventory destinationInventory = inventoryRepository.findLocked(destinationStoreId, variantId)
                .orElseGet(() -> new StoreInventory(destination, variant));

        decreaseOrThrow(sourceInventory, quantity);
        destinationInventory.receive(quantity);
        inventoryRepository.save(sourceInventory);
        inventoryRepository.save(destinationInventory);

        return new InventoryTransferResult(
                sourceStoreId,
                destinationStoreId,
                variantId,
                quantity,
                sourceInventory.getQuantity(),
                destinationInventory.getQuantity(),
                OffsetDateTime.now(ZoneOffset.UTC));
    }

    private Store requireActiveStore(Long storeId) {
        Store store = storeRepository.findLockedById(storeId).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND, "STORE_NOT_FOUND", "Không tìm thấy cửa hàng"));
        if (!store.isActive()) {
            throw new ApiException(HttpStatus.CONFLICT, "STORE_INACTIVE", "Cửa hàng đang ngừng hoạt động");
        }
        return store;
    }

    private ProductVariant requireActiveVariant(Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND, "VARIANT_NOT_FOUND", "Không tìm thấy biến thể sản phẩm"));
        if (!variant.isActive()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "VARIANT_NOT_FOUND", "Không tìm thấy biến thể sản phẩm");
        }
        return variant;
    }

    private StoreInventory requireInventory(Long storeId, Long variantId) {
        return inventoryRepository.findLocked(storeId, variantId).orElseThrow(() -> insufficientStock());
    }

    private void decreaseOrThrow(StoreInventory inventory, int quantity) {
        if (inventory.getQuantity() < quantity) {
            throw insufficientStock();
        }
        inventory.decrease(quantity);
    }

    private void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw validation("Số lượng phải lớn hơn 0");
        }
    }

    private void requireIds(Long... ids) {
        for (Long id : ids) {
            if (id == null || id <= 0) {
                throw validation("ID cửa hàng và biến thể phải là số dương");
            }
        }
    }

    private ApiException validation(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
    }

    private ApiException insufficientStock() {
        return new ApiException(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", "Cửa hàng nguồn không đủ tồn kho");
    }
}
