package com.utegear.dto.inventory;

import com.utegear.entity.ProductVariant;
import com.utegear.entity.Store;
import com.utegear.entity.StoreInventory;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public final class AvailabilityDtos {

    private AvailabilityDtos() {
    }

    public record StoreReference(Long id, String name, boolean active) {
        public static StoreReference from(Store store) {
            return new StoreReference(store.getId(), store.getName(), store.isActive());
        }
    }

    public record VariantAvailability(
            StoreReference store,
            Long variantId,
            String sku,
            Map<String, String> optionValues,
            int quantity,
            boolean available,
            OffsetDateTime updatedAt) {

        public static VariantAvailability from(
                Store store,
                ProductVariant variant,
                StoreInventory inventory) {
            int quantity = inventory == null ? 0 : inventory.getQuantity();
            return new VariantAvailability(
                    StoreReference.from(store), variant.getId(), variant.getSku(),
                    variant.getOptionValues(), quantity, store.isActive() && quantity > 0,
                    inventory == null ? null : inventory.getUpdatedAt());
        }
    }

    public record AvailableStore(
            Long storeId,
            String storeName,
            int quantity,
            boolean active) {

        public static AvailableStore from(StoreInventory inventory) {
            Store store = inventory.getStore();
            return new AvailableStore(
                    store.getId(), store.getName(), inventory.getQuantity(), store.isActive());
        }
    }

    public record VariantStock(
            Long variantId,
            String sku,
            Map<String, String> optionValues,
            boolean isDefault,
            long totalStock,
            List<AvailableStore> availableStores) {
    }

    public record ProductAvailability(
            Long productId,
            long totalStock,
            List<VariantStock> variants) {
    }
}
