package com.utegear.repository;

import com.utegear.entity.StoreInventory;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreInventoryRepository extends JpaRepository<StoreInventory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select inventory
            from StoreInventory inventory
            where inventory.store.id = :storeId and inventory.variant.id = :variantId
            """)
    Optional<StoreInventory> findLocked(
            @Param("storeId") Long storeId,
            @Param("variantId") Long variantId);

    Optional<StoreInventory> findByStoreIdAndVariantId(Long storeId, Long variantId);

    boolean existsByStoreIdAndVariantId(Long storeId, Long variantId);

    @Query("""
            select inventory.variant.id as variantId, sum(inventory.quantity) as quantity
            from StoreInventory inventory
            where inventory.store.active = true and inventory.variant.id in :variantIds
            group by inventory.variant.id
            """)
    List<VariantStock> sumStockByVariantIds(@Param("variantIds") Collection<Long> variantIds);

    interface VariantStock {
        Long getVariantId();
        Long getQuantity();
    }
}
