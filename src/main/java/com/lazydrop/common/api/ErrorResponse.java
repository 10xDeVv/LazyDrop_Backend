package com.lazydrop.common.api;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        String code,
        String message
) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(Instant.now(), code, message);
    }
}
