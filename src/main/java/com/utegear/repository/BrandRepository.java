package com.utegear.repository;
import com.utegear.entity.Brand; import java.util.List; import org.springframework.data.jpa.repository.JpaRepository;
public interface BrandRepository extends JpaRepository<Brand,Long>{ boolean existsByNameIgnoreCase(String name); boolean existsBySlug(String slug); List<Brand> findByActiveTrueOrderByNameAsc(); }
