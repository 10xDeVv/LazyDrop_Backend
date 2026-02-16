package com.lazydrop.modules.websocket.payload;

import java.time.Instant;

public record DropSessionCreatedPayload(
        String sessionId,
        String ownerUserId,
        Instant createdAt,
        long expiresInSeconds
) {
}
