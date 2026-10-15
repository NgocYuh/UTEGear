package com.utegear.service;

import com.utegear.dto.inventory.InventoryResult;
import com.utegear.dto.inventory.InventoryTransferResult;
import com.utegear.entity.Order;
import com.utegear.entity.OrderItem;
import com.utegear.entity.ProductVariant;
import com.utegear.entity.Store;
import com.utegear.entity.StoreInventory;
import com.utegear.exception.ApiException;
import com.utegear.repository.ProductVariantRepository;
import com.utegear.repository.StoreInventoryRepository;
import com.utegear.repository.StoreRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
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

    @Transactional(readOnly = true)
    public List<com.utegear.dto.admin.AdminDtos.InventoryView> findAll(Long storeId) {
        List<StoreInventory> inventories = storeId == null
                ? inventoryRepository.findAllByOrderByStoreNameAscVariantProductNameAscVariantSkuAsc()
                : inventoryRepository.findByStoreIdOrderByVariantProductNameAscVariantSkuAsc(storeId);
        return inventories.stream().map(com.utegear.dto.admin.AdminDtos.InventoryView::from).toList();
    }

    @Transactional
    public InventoryResult setQuantity(Long storeId, Long variantId, int quantity) {
        requireIds(storeId, variantId);
        if (quantity < 0) {
            throw validation("Tồn kho phải là số nguyên không âm");
        }
        Store store = requireActiveStore(storeId);
        ProductVariant variant = requireActiveVariant(variantId);
        StoreInventory inventory = inventoryRepository.findLocked(storeId, variantId)
                .orElseGet(() -> new StoreInventory(store, variant));
        inventory.setQuantity(quantity);
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

    @Transactional
    public void restoreReservedStock(Order order) {
        if (order == null || order.getFulfillmentStore() == null || order.getFulfillmentStore().getId() == null) {
            throw new ApiException(HttpStatus.CONFLICT, "ORDER_STORE_NOT_FOUND",
                    "Đơn hàng không có cửa hàng thực hiện để hoàn tồn kho");
        }
        Long storeId = order.getFulfillmentStore().getId();
        order.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getVariant().getId()))
                .forEach(item -> restoreLine(storeId, item));
    }

    private void restoreLine(Long storeId, OrderItem item) {
        Long variantId = item.getVariant().getId();
        StoreInventory inventory = inventoryRepository.findLocked(storeId, variantId)
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "INVENTORY_RESERVATION_NOT_FOUND",
                        "Không tìm thấy tồn kho đã giữ cho đơn hàng"));
        inventory.receive(item.getQuantity());
        inventoryRepository.save(inventory);
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
