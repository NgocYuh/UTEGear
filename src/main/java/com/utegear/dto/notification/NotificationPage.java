package com.utegear.dto.notification;

import com.utegear.entity.Notification;
import java.util.List;
import org.springframework.data.domain.Page;

public record NotificationPage(List<NotificationResponse> content, int page, int size, long totalElements,
                               int totalPages, boolean first, boolean last, boolean empty) {

    public static NotificationPage from(Page<Notification> source) {
        return new NotificationPage(source.getContent().stream().map(NotificationResponse::from).toList(),
                source.getNumber(), source.getSize(), source.getTotalElements(), source.getTotalPages(),
                source.isFirst(), source.isLast(), source.isEmpty());
    }
}
