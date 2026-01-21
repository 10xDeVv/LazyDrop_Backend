package com.lazydrop.modules.websocket.payload;

import java.time.Instant;

public record FileUploadedPayload(
        String fileId,
        String originalName,
        long sizeBytes,
        String uploaderUserId,
        Instant createdAt
) {}
