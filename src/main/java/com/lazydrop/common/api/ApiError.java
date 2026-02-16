package com.lazydrop.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String requestId,
        Map<String, Object> details
) {
    public static ApiError of(int status, String error, String message, String requestId) {
        return new ApiError(Instant.now(), status, error, message, requestId, null);
    }

    public static ApiError of(int status, String error, String message, String requestId, Map<String, Object> details) {
        return new ApiError(Instant.now(), status, error, message, requestId, details);
    }
}
