package com.utegear.repository;

import com.utegear.entity.ProductImage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    Optional<ProductImage> findByIdAndProductId(Long id, Long productId);

    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);
}
