package com.lazydrop.modules.websocket.payload;

import java.time.Instant;

public record FileDownloadedPayload(
        String fileId,
        String downloaderUserId,
        Instant downloadedAt
) {}
