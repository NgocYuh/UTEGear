package com.utegear.repository;

import com.utegear.entity.SupportConversation;
import com.utegear.entity.SupportConversationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface SupportConversationRepository extends JpaRepository<SupportConversation, Long> {
    Optional<SupportConversation> findByCustomerIdAndStoreIdAndStatus(
            Long customerId, Long storeId, SupportConversationStatus status);

    List<SupportConversation> findAllByCustomerIdOrderByUpdatedAtDesc(Long customerId);

    List<SupportConversation> findAllByEmployeeIdOrderByUpdatedAtDesc(Long employeeId);

    List<SupportConversation> findAllByStoreIdAndEmployeeIsNullAndStatusOrderByUpdatedAtDesc(
            Long storeId, SupportConversationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select conversation from SupportConversation conversation where conversation.id = :id")
    Optional<SupportConversation> findLockedById(@Param("id") Long id);
}
