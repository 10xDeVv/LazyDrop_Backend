package com.lazydrop.modules.session.participant.dto;

import lombok.Builder;

@Builder
public record ParticipantDto(
        String id,
        String userId,
        String role,
        boolean autoDownload,
        String displayName,
        boolean guest
) {}
