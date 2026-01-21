package com.lazydrop.modules.session.note.dto;

import java.time.Instant;

public record SessionNoteDto(
        String id,
        String senderId,
        String content,
        Instant createdAt
) {
}
