package com.utegear.repository;
import com.utegear.entity.Category; import java.util.List; import java.util.Optional; import org.springframework.data.jpa.repository.JpaRepository;
public interface CategoryRepository extends JpaRepository<Category,Long>{ boolean existsByNameIgnoreCase(String name); boolean existsBySlug(String slug); Optional<Category> findBySlug(String slug); List<Category> findByActiveTrueOrderBySortOrderAscNameAsc(); }
