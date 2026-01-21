package com.lazydrop.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends LazyDropException {
    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message);
    }
}
