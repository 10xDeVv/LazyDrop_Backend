package com.lazydrop.modules.session.core.mapper;

import com.lazydrop.modules.session.core.dto.DropSessionResponse;
import com.lazydrop.modules.session.core.model.DropSession;
import com.lazydrop.modules.user.model.User;

import java.time.Instant;

public class DropSessionMapper {

    public static DropSessionResponse toDropSessionResponse(DropSession session, User me) {
        boolean isOwner = session.getOwner().getId().equals(me.getId());
       return DropSessionResponse
                .builder()
                .code(session.getCode())
                .id(session.getId().toString())
                .ownerId(session.getOwner().getId().toString())
                .expiresAt(session.getExpiresAt())
                .remainingSeconds(session.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond())
                .status(session.getStatus())
               .myRole(isOwner ? "OWNER" : "PEER")
                .build();
    }
}
