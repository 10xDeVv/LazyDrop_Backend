package com.lazydrop.modules.session.core.event;

import com.lazydrop.modules.session.core.model.SessionEndReason;

import java.util.UUID;

public class DropSessionEndedEvent {
    private final UUID sessionId;
    private final SessionEndReason reason;

    public DropSessionEndedEvent(UUID sessionId, SessionEndReason reason) {
        this.sessionId = sessionId;
        this.reason = reason;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public SessionEndReason getReason() {
        return reason;
    }
}
