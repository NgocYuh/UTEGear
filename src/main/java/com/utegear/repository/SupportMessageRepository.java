package com.utegear.repository;

import com.utegear.entity.SupportMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {
    List<SupportMessage> findAllByConversationIdOrderBySentAtAsc(Long conversationId);
}
