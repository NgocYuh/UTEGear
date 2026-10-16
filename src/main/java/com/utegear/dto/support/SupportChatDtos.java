package com.utegear.dto.support;

import com.utegear.entity.SupportConversation;
import com.utegear.entity.SupportConversationStatus;
import com.utegear.entity.SupportMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

public final class SupportChatDtos {
    private SupportChatDtos() { }
    public record StartConversationRequest(@NotNull @Positive Long storeId) { }
    public record SendMessageRequest(@NotBlank @Size(max = 2000) String content) { }
    public record ConversationView(Long id, SupportConversationStatus status, Long storeId, String storeName,
            Long customerId, String customerName, Long employeeId, String employeeName,
            OffsetDateTime createdAt, OffsetDateTime updatedAt, OffsetDateTime closedAt) {
        public static ConversationView from(SupportConversation value) {
            return new ConversationView(value.getId(), value.getStatus(), value.getStore().getId(), value.getStore().getName(),
                    value.getCustomer().getId(), value.getCustomer().getFullName(),
                    value.getEmployee() == null ? null : value.getEmployee().getId(),
                    value.getEmployee() == null ? null : value.getEmployee().getFullName(),
                    value.getCreatedAt(), value.getUpdatedAt(), value.getClosedAt());
        }
    }
    public record MessageView(Long id, Long conversationId, Long senderId, String senderName,
            String content, OffsetDateTime sentAt) {
        public static MessageView from(SupportMessage value) {
            return new MessageView(value.getId(), value.getConversation().getId(), value.getSender().getId(),
                    value.getSender().getFullName(), value.getContent(), value.getSentAt());
        }
    }
    public record ConversationDetail(ConversationView conversation, List<MessageView> messages) { }
}
