package com.utegear.repository;

import com.utegear.entity.Review;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"user", "images"})
    @Query("select r from Review r where r.product.id = :productId order by r.createdAt desc")
    List<Review> findByProductIdWithUserAndImages(@Param("productId") Long productId);

    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    long countByProductId(Long productId);

    @Query("select coalesce(avg(r.rating), 0.0) from Review r where r.product.id = :productId")
    Double calculateAverageRating(@Param("productId") Long productId);

    @Query("select r.rating, count(r) from Review r where r.product.id = :productId group by r.rating")
    List<Object[]> countByRatingGroup(@Param("productId") Long productId);
}

