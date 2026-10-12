package com.utegear.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class StoreInventoryTest {

    @Test
    void preventsNegativeInventory() {
        StoreInventory inventory = inventory();
        inventory.receive(3);

        assertThatThrownBy(() -> inventory.decrease(4))
                .isInstanceOf(IllegalStateException.class);
        assertThat(inventory.getQuantity()).isEqualTo(3);
    }

    @Test
    void mapsUniqueStoreAndVariantConstraint() {
        Table table = StoreInventory.class.getAnnotation(Table.class);

        assertThat(table.name()).isEqualTo("store_inventory");
        assertThat(table.uniqueConstraints()).hasSize(1);
        assertThat(Set.of(table.uniqueConstraints()[0].columnNames()))
                .containsExactlyInAnyOrder("store_id", "variant_id");
    }

    private StoreInventory inventory() {
        Store store = new Store(
                "UTEGear", "01 Võ Văn Ngân", "TP.HCM", null, null, null, null,
                LocalTime.of(8, 0), LocalTime.of(21, 0), null, null, null, true);
        ProductVariant variant = new ProductVariant("SKU-01", Map.of(), true, BigDecimal.ZERO);
        return new StoreInventory(store, variant);
    }
}
