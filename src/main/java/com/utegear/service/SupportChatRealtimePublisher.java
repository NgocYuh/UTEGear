package com.utegear.service;

import com.utegear.dto.realtime.RealtimeEvent;
import com.utegear.dto.support.SupportChatDtos;
import com.utegear.entity.SupportConversation;
import com.utegear.entity.SupportMessage;
import com.utegear.util.TransactionCallbacks;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SupportChatRealtimePublisher {
    public static final String USER_SUPPORT_DESTINATION = "/queue/support";
    private final SimpMessagingTemplate messaging;
    private final TransactionCallbacks transactionCallbacks;
    public SupportChatRealtimePublisher(SimpMessagingTemplate messaging, TransactionCallbacks transactionCallbacks) {
        this.messaging = messaging; this.transactionCallbacks = transactionCallbacks;
    }
    public void publishMessageAfterCommit(SupportMessage message) {
        RealtimeEvent<SupportChatDtos.MessageView> event = envelope("SUPPORT_MESSAGE_CREATED", SupportChatDtos.MessageView.from(message));
        SupportConversation conversation = message.getConversation();
        transactionCallbacks.afterCommit(() -> {
            messaging.convertAndSendToUser(conversation.getCustomer().getEmail(), USER_SUPPORT_DESTINATION, event);
            if (conversation.getEmployee() != null) messaging.convertAndSendToUser(conversation.getEmployee().getEmail(), USER_SUPPORT_DESTINATION, event);
        });
    }
    public void publishAssignedAfterCommit(SupportConversation conversation) {
        if (conversation.getEmployee() == null) return;
        RealtimeEvent<SupportChatDtos.ConversationView> event = envelope("SUPPORT_CONVERSATION_ASSIGNED", SupportChatDtos.ConversationView.from(conversation));
        transactionCallbacks.afterCommit(() -> messaging.convertAndSendToUser(conversation.getEmployee().getEmail(), USER_SUPPORT_DESTINATION, event));
    }
    private <T> RealtimeEvent<T> envelope(String type, T data) {
        return new RealtimeEvent<>("evt_" + UUID.randomUUID(), type, OffsetDateTime.now(ZoneOffset.UTC), data);
    }
}
