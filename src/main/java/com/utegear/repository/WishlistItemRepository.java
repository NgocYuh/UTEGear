package com.utegear.repository;

import com.utegear.entity.WishlistItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    @EntityGraph(attributePaths = "product")
    List<WishlistItem> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = "product")
    Optional<WishlistItem> findByUserIdAndProductId(Long userId, Long productId);
}
