package com.lazydrop.modules.websocket;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Setter
@Getter
public class WebSocketMessage<T> {
    private MessageType type;
    private T payload;
    private Instant timestamp;

    public WebSocketMessage() {}

    public WebSocketMessage(MessageType type, T payload) {
        this.type = type;
        this.payload = payload;
        this.timestamp = Instant.now();
    }

}
