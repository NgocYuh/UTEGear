package com.utegear.repository;
import com.utegear.entity.Product;
import com.utegear.entity.ProductStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByIdAndStatus(Long id, ProductStatus status);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
    long countByStatus(ProductStatus status);

    @Query("""
            select distinct product from Product product
            join fetch product.category
            left join fetch product.brand
            left join fetch product.variants
            where product.status = :status and exists (
                select inventory.id from StoreInventory inventory
                where inventory.variant.product = product
                  and inventory.store.id = :storeId and inventory.store.active = true
                  and inventory.variant.active = true and inventory.quantity > 0
            )
            order by product.name, product.id
            """)
    List<Product> findInStockAtStore(@Param("storeId") Long storeId, @Param("status") ProductStatus status);

    // Load the second list separately: Hibernate cannot fetch two bags in one query.
    @Query("""
            select distinct product from Product product
            left join fetch product.images
            where product.id in :ids
            """)
    List<Product> findWithImagesByIds(@Param("ids") Collection<Long> ids);
}
