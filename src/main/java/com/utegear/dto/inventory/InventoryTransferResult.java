package com.utegear.dto.inventory;

import java.time.OffsetDateTime;

public record InventoryTransferResult(
        Long sourceStoreId,
        Long destinationStoreId,
        Long variantId,
        int quantity,
        int sourceQuantity,
        int destinationQuantity,
        OffsetDateTime transferredAt) {
}
