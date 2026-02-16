package com.lazydrop.modules.websocket.payload;

import java.time.Instant;

public record DropSessionExpiredPayload(
        String sessionId,
        String ownerId,
        Instant expiresAt
) {
}
