package com.lazydrop.modules.session.note.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateSessionNoteRequest(
        @NotBlank(message = "Message cannot be empty") String content,
        String clientNoteId
) {
}
