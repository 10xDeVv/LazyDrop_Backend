package com.lazydrop.modules.session.core.mapper;

import com.lazydrop.modules.session.core.dto.DropSessionResponse;
import com.lazydrop.modules.session.core.model.DropSession;

import java.time.Instant;

public class DropSessionMapper {

    public static DropSessionResponse toDropSessionResponse(DropSession session) {
       return DropSessionResponse
                .builder()
                .code(session.getCode())
                .id(session.getId().toString())
                .ownerId(session.getOwner().getId().toString())
                .expiresAt(session.getExpiresAt())
                .remainingSeconds(session.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond())
                .status(session.getStatus())
                .build();
    }
}
