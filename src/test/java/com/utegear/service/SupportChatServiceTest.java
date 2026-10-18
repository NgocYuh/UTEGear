package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.dto.support.SupportChatDtos;
import com.utegear.entity.Role;
import com.utegear.entity.Store;
import com.utegear.entity.SupportConversation;
import com.utegear.entity.SupportConversationStatus;
import com.utegear.entity.SupportMessage;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.StoreRepository;
import com.utegear.repository.SupportConversationRepository;
import com.utegear.repository.SupportMessageRepository;
import com.utegear.repository.UserRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SupportChatServiceTest {

    private SupportConversationRepository conversations;
    private SupportMessageRepository messages;
    private StoreRepository stores;
    private UserRepository users;
    private SupportChatRealtimePublisher realtime;
    private SupportChatService service;

    private User customer;
    private User employee;
    private Store store;
    private SupportConversation conversation;

    @BeforeEach
    void setUp() {
        conversations = mock(SupportConversationRepository.class);
        messages = mock(SupportMessageRepository.class);
        stores = mock(StoreRepository.class);
        users = mock(UserRepository.class);
        realtime = mock(SupportChatRealtimePublisher.class);
        service = new SupportChatService(conversations, messages, stores, users, realtime);

        customer = new User("Customer Test", "customer@example.com", "0900000001", "hash");
        customer.getRoles().add(new Role(Role.CUSTOMER));
        ReflectionTestUtils.setField(customer, "id", 101L);

        store = new Store("UTEGear Q1", "01 Nguyen Hue", "TP.HCM", "Quan 1",
                "0900000002", "q1@utegear.vn", null, LocalTime.of(8, 0), LocalTime.of(21, 0),
                null, null, null, true);
        ReflectionTestUtils.setField(store, "id", 1L);

        employee = new User("Employee Test", "employee@example.com", "0900000003", "hash");
        employee.getRoles().add(new Role(Role.EMPLOYEE));
        employee.setAssignedStore(store);
        ReflectionTestUtils.setField(employee, "id", 201L);

        conversation = new SupportConversation(customer, store, employee);
        ReflectionTestUtils.setField(conversation, "id", 501L);

        when(stores.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(store));
        when(conversations.findLockedById(501L)).thenReturn(Optional.of(conversation));
        when(conversations.findById(501L)).thenReturn(Optional.of(conversation));
        when(conversations.save(any(SupportConversation.class))).thenAnswer(inv -> inv.getArgument(0));
        when(messages.save(any(SupportMessage.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void customerCanStartConversationWithActiveStore() {
        when(conversations.findByCustomerIdAndStoreIdAndStatus(101L, 1L, SupportConversationStatus.OPEN))
                .thenReturn(Optional.empty());
        when(users.findEnabledByAssignedStoreIdAndRoleName(1L, Role.EMPLOYEE))
                .thenReturn(List.of(employee));

        SupportChatDtos.ConversationView view = service.start(customer, 1L);

        assertThat(view.storeId()).isEqualTo(1L);
        assertThat(view.customerId()).isEqualTo(101L);
        assertThat(view.status()).isEqualTo(SupportConversationStatus.OPEN);
        verify(conversations).save(any(SupportConversation.class));
    }

    @Test
    void customerCanSendMessageWithinValidLength() {
        SupportChatDtos.MessageView view = service.send(customer, 501L, "Xin chào hỗ trợ");

        assertThat(view.content()).isEqualTo("Xin chào hỗ trợ");
        assertThat(view.senderId()).isEqualTo(101L);
        verify(messages).save(any(SupportMessage.class));
    }

    @Test
    void rejectsMessageContentBlankOrOverMaxChars() {
        assertThatThrownBy(() -> service.send(customer, 501L, ""))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");

        assertThatThrownBy(() -> service.send(customer, 501L, "   "))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");

        String overMax = "x".repeat(2001);
        assertThatThrownBy(() -> service.send(customer, 501L, overMax))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void rejectsSendingMessageToClosedConversation() {
        conversation.close();

        assertThatThrownBy(() -> service.send(customer, 501L, "Tin nhắn khi đã đóng"))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void employeeCanClaimUnassignedConversationForAssignedStore() {
        SupportConversation unassigned = new SupportConversation(customer, store, null);
        ReflectionTestUtils.setField(unassigned, "id", 502L);
        when(conversations.findLockedById(502L)).thenReturn(Optional.of(unassigned));

        SupportChatDtos.ConversationView claimed = service.claim(employee, 502L);

        assertThat(claimed.employeeId()).isEqualTo(201L);
        verify(conversations).save(unassigned);
    }

    @Test
    void employeeCannotClaimConversationForDifferentStore() {
        Store otherStore = new Store("UTEGear HN", "01 Ba Trieu", "Ha Noi", "Hai Ba Trung",
                "0900000009", "hn@utegear.vn", null, LocalTime.of(8, 0), LocalTime.of(21, 0),
                null, null, null, true);
        ReflectionTestUtils.setField(otherStore, "id", 2L);

        SupportConversation otherStoreConv = new SupportConversation(customer, otherStore, null);
        ReflectionTestUtils.setField(otherStoreConv, "id", 503L);
        when(conversations.findLockedById(503L)).thenReturn(Optional.of(otherStoreConv));

        assertThatThrownBy(() -> service.claim(employee, 503L))
                .isInstanceOf(ApiException.class)
                .extracting("code")
                .isEqualTo("FORBIDDEN");
    }

    @Test
    void customerCanCloseConversation() {
        SupportChatDtos.ConversationView closed = service.close(customer, 501L);

        assertThat(closed.status()).isEqualTo(SupportConversationStatus.CLOSED);
        verify(conversations).save(conversation);
    }
}

