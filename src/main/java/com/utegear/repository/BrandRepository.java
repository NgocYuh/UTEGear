package com.utegear.repository;
import com.utegear.entity.Brand; import java.util.List; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository;
public interface BrandRepository extends JpaRepository<Brand,Long>{ boolean existsByNameIgnoreCase(String name); boolean existsByNameIgnoreCaseAndIdNot(String name,Long id); boolean existsBySlug(String slug); boolean existsBySlugAndIdNot(String slug,Long id); Optional<Brand> findByNameIgnoreCase(String name); List<Brand> findByActiveTrueOrderByNameAsc(); List<Brand> findAllByOrderByNameAsc(); }
