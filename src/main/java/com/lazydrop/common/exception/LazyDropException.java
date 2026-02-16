package com.lazydrop.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class LazyDropException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    protected LazyDropException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

}
