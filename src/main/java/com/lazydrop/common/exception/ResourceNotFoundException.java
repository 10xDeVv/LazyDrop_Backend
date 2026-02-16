package com.lazydrop.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends LazyDropException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
    }
}

