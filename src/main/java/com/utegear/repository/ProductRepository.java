package com.utegear.repository;
import com.utegear.entity.Product; import com.utegear.entity.ProductStatus; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface ProductRepository extends JpaRepository<Product,Long>,JpaSpecificationExecutor<Product>{Optional<Product> findByIdAndStatus(Long id,ProductStatus status); boolean existsBySlug(String slug);}
