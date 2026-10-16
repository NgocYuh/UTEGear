package com.utegear.demo;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

/** Caller supplies a development-only transaction and write lock. Never resets existing stock. */
public final class DemoBranchInventory {
    private DemoBranchInventory() { }

    public record Result(long storeId, int insertedVariants, long totalVariants, long totalQuantity) { }

    public static List<Result> provision(JdbcTemplate jdbc, int quantity) {
        if (quantity < 1 || quantity > 1000) throw new IllegalArgumentException("Quantity must be between 1 and 1000");
        if (jdbc.queryForObject("SELECT count(*) FROM stores WHERE active=true AND ((id=1) OR (id=2 AND email='hanoi@demo.utegear.test') OR (id=3 AND email='danang@demo.utegear.test'))", Long.class) != 3) {
            throw new IllegalStateException("Expected the confirmed CN-1/CN-2/CN-3 development stores");
        }
        if (jdbc.queryForObject("SELECT count(*) FROM store_inventory WHERE store_id=1", Long.class) == 0) {
            throw new IllegalStateException("CN-1 has no source variants");
        }
        return List.of(provisionStore(jdbc, 2, quantity), provisionStore(jdbc, 3, quantity));
    }

    private static Result provisionStore(JdbcTemplate jdbc, long storeId, int quantity) {
        int inserted = jdbc.update("""
                INSERT INTO store_inventory(store_id,variant_id,quantity,updated_at)
                SELECT ?, source.variant_id, ?, CURRENT_TIMESTAMP
                FROM store_inventory source
                JOIN product_variants variant ON variant.id=source.variant_id
                JOIN products product ON product.id=variant.product_id
                WHERE source.store_id=1 AND variant.active=true AND product.status='ACTIVE'
                  AND NOT EXISTS (SELECT 1 FROM store_inventory target
                                  WHERE target.store_id=? AND target.variant_id=source.variant_id)
                """, storeId, quantity, storeId);
        return jdbc.queryForObject("SELECT count(*) variants, COALESCE(sum(quantity),0) quantity FROM store_inventory WHERE store_id=?",
                (row, index) -> new Result(storeId, inserted, row.getLong("variants"), row.getLong("quantity")), storeId);
    }
}
