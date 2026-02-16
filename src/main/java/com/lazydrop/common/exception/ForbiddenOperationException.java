package com.lazydrop.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenOperationException extends LazyDropException {
    public ForbiddenOperationException(String message) {
        super(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
    }
}

