package com.lazydrop.modules.websocket.payload;

public record PeerLeftPayload(
        String participantId,
        String userId
) {}
