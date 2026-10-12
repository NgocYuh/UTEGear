package com.utegear.service;

import com.utegear.dto.notification.NotificationPage;
import com.utegear.dto.notification.NotificationResponse;
import com.utegear.entity.Notification;
import com.utegear.entity.NotificationType;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Transactional
    public Notification create(User user, NotificationType type, String title, String message, String referenceId) {
        requireUser(user);
        return notifications.save(new Notification(user, type, title, message, referenceId));
    }

    @Transactional(readOnly = true)
    public NotificationPage list(User user, Boolean read, int page, int size) {
        requireUser(user);
        if (page < 0 || size < 1 || size > 100) {
            throw validation("Page phải từ 0 và size phải từ 1 đến 100");
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> result = read == null
                ? notifications.findAllByUserId(user.getId(), pageable)
                : notifications.findAllByUserIdAndRead(user.getId(), read, pageable);
        return NotificationPage.from(result);
    }

    @Transactional
    public NotificationResponse markRead(User user, Long notificationId) {
        requireUser(user);
        Notification notification = notifications.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(this::notFound);
        notification.markRead();
        return NotificationResponse.from(notifications.save(notification));
    }

    @Transactional
    public void markAllRead(User user) {
        requireUser(user);
        PageRequest batch = PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "id"));
        Page<Notification> unread;
        do {
            unread = notifications.findAllByUserIdAndRead(user.getId(), false, batch);
            unread.getContent().forEach(Notification::markRead);
            notifications.saveAll(unread.getContent());
        } while (unread.hasNext());
    }

    private void requireUser(User user) {
        if (user == null || user.getId() == null) {
            throw validation("Người dùng hợp lệ là bắt buộc");
        }
    }

    private ApiException validation(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
    }

    private ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND", "Không tìm thấy thông báo");
    }
}
