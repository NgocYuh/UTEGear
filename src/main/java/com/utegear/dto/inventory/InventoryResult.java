package com.utegear.dto.inventory;

import com.utegear.entity.StoreInventory;
import java.time.OffsetDateTime;

public record InventoryResult(
        Long storeId,
        Long variantId,
        int quantity,
        OffsetDateTime updatedAt) {

    public static InventoryResult from(StoreInventory inventory) {
        return new InventoryResult(
                inventory.getStore().getId(),
                inventory.getVariant().getId(),
                inventory.getQuantity(),
                inventory.getUpdatedAt());
    }
}
