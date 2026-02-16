package com.lazydrop.modules.websocket.payload;

import java.time.Instant;

public record DropSessionClosedPayload(
        String sessionId,
        String ownerId,
        Instant expiresAt
) {
}
