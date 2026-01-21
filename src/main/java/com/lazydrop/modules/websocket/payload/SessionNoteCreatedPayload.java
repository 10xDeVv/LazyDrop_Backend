package com.lazydrop.modules.websocket.payload;

import java.time.Instant;

public record SessionNoteCreatedPayload(
        String noteId,
        String senderId,
        String content,
        Instant createdAt,
        String clientNoteId
) {
}
