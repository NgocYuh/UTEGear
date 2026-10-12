package com.utegear.dto.realtime;

import java.time.OffsetDateTime;

public record RealtimeEvent<T>(String eventId, String type, OffsetDateTime occurredAt, T data) {
}
