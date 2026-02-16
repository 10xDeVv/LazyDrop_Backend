package com.lazydrop.common.exception;

import org.springframework.http.HttpStatus;

public class DropSessionExpiredException extends LazyDropException {
    public DropSessionExpiredException(String message) {
        super(HttpStatus.GONE, "DROP_SESSION_EXPIRED", message);
    }
}

