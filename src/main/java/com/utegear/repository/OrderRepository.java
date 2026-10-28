package com.utegear.repository;

import com.utegear.entity.Order;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    long countByStatus(com.utegear.entity.OrderStatus status);

    Optional<Order> findByOrderCode(String orderCode);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    boolean existsByOrderCode(String orderCode);

    @EntityGraph(attributePaths = {"items", "items.variant", "payment", "fulfillmentStore"})
    @Query("select order from Order order where order.id = :id")
    Optional<Order> findDetailedById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"items", "items.variant", "payment", "fulfillmentStore"})
    @Query("select order from Order order where order.id = :id and order.user.id = :userId")
    Optional<Order> findDetailedByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select order from Order order where order.id = :id")
    Optional<Order> findLockedById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select order from Order order where order.id = :id and order.user.id = :userId")
    Optional<Order> findLockedByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("""
            select count(o) > 0 from Order o
            join o.items item
            where o.user.id = :userId
              and o.status = com.utegear.entity.OrderStatus.COMPLETED
              and item.variant.product.id = :productId
            """)
    boolean hasCompletedOrderWithProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    @Query("""
            select coalesce(sum(o.total), 0) from Order o
            where o.status = com.utegear.entity.OrderStatus.COMPLETED
               or (o.payment is not null and o.payment.status = com.utegear.entity.PaymentStatus.PAID)
            """)
    java.math.BigDecimal sumTotalRevenue();
}
