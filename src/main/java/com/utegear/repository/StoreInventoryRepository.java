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

    @Query("""
            select inventory
            from StoreInventory inventory
            join fetch inventory.store store
            join fetch inventory.variant variant
            join fetch variant.product product
            where product.id = :productId
              and store.active = true
              and variant.active = true
            """)
    List<StoreInventory> findProductAvailability(@Param("productId") Long productId);

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

    List<StoreInventory> findByStoreIdOrderByVariantProductNameAscVariantSkuAsc(Long storeId);

    List<StoreInventory> findAllByOrderByStoreNameAscVariantProductNameAscVariantSkuAsc();

    @Query("""
            select inventory.variant.id as variantId, sum(inventory.quantity) as quantity
            from StoreInventory inventory
            where inventory.store.active = true and inventory.variant.id in :variantIds
            group by inventory.variant.id
            """)
    List<VariantStock> sumStockByVariantIds(@Param("variantIds") Collection<Long> variantIds);

    @Query("""
            select inventory
            from StoreInventory inventory
            join fetch inventory.store store
            join fetch inventory.variant variant
            join fetch variant.product product
            where product.id = :productId
              and store.active = true
              and variant.active = true
              and lower(store.province) = lower(:province)
            """)
    List<StoreInventory> findProductAvailability(
            @Param("productId") Long productId,
            @Param("province") String province);

    interface VariantStock {
        Long getVariantId();
        Long getQuantity();
    }

    @Query("""
            select inventory.variant.id as variantId, sum(inventory.quantity) as quantity
            from StoreInventory inventory
            where inventory.store.id = :storeId and inventory.store.active = true
              and inventory.variant.active = true and inventory.variant.id in :variantIds
            group by inventory.variant.id
            """)
    List<VariantStock> sumStoreStockByVariantIds(@Param("storeId") Long storeId,
            @Param("variantIds") Collection<Long> variantIds);
}
