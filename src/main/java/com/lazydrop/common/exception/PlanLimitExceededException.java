package com.lazydrop.common.exception;

import org.springframework.http.HttpStatus;

public class PlanLimitExceededException extends LazyDropException {
    public PlanLimitExceededException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, "PLAN_LIMIT_EXCEEDED", message);
    }
}

