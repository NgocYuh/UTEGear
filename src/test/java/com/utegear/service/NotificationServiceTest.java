package com.utegear.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utegear.entity.Notification;
import com.utegear.entity.NotificationType;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.NotificationRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

class NotificationServiceTest {

    private NotificationRepository repository;
    private NotificationService service;
    private User owner;
    private Notification notification;

    @BeforeEach
    void setUp() {
        repository = mock(NotificationRepository.class);
        service = new NotificationService(repository);
        owner = user(7L, "owner@example.test");
        notification = new Notification(owner, NotificationType.ORDER_STATUS_CHANGED,
                "Đơn hàng đã xác nhận", "Đơn UTE-0001 đã được xác nhận.", "20");
        ReflectionTestUtils.setField(notification, "id", 30L);
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void listsOnlyCurrentUsersNotifications() {
        when(repository.findAllByUserIdAndRead(org.mockito.ArgumentMatchers.eq(7L),
                org.mockito.ArgumentMatchers.eq(false), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notification)));

        var result = service.list(owner, false, 0, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().id()).isEqualTo(30L);
    }

    @Test
    void marksOwnedNotificationAsRead() {
        when(repository.findByIdAndUserId(30L, 7L)).thenReturn(Optional.of(notification));

        var result = service.markRead(owner, 30L);

        assertThat(result.read()).isTrue();
        verify(repository).save(notification);
    }

    @Test
    void cannotReadAnotherUsersNotification() {
        when(repository.findByIdAndUserId(30L, 8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markRead(user(8L, "other@example.test"), 30L))
                .isInstanceOf(ApiException.class)
                .extracting("code").isEqualTo("NOTIFICATION_NOT_FOUND");
    }

    @Test
    void marksAllOwnedUnreadNotifications() {
        when(repository.findAllByUserIdAndRead(org.mockito.ArgumentMatchers.eq(7L),
                org.mockito.ArgumentMatchers.eq(false), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(notification)));

        service.markAllRead(owner);

        assertThat(notification.isRead()).isTrue();
        verify(repository).saveAll(List.of(notification));
    }

    @Test
    void createsNotificationForPersistedUser() {
        Notification saved = service.create(owner, NotificationType.ORDER_STATUS_CHANGED,
                "Đơn hàng đang giao", "Đơn UTE-0001 đang được giao.", "20");

        assertThat(saved.getUser()).isSameAs(owner);
        assertThat(saved.getReferenceId()).isEqualTo("20");
    }

    private User user(Long id, String email) {
        User user = new User("Test User", email, null, "hash");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
