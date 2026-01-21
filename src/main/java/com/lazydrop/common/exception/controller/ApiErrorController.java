package com.lazydrop.common.exception.controller;

import com.lazydrop.common.api.ApiError;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/error")
public class ApiErrorController implements ErrorController {

    @RequestMapping
    public ResponseEntity<ApiError> handleError(HttpServletRequest request) {
        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusAttr != null ? Integer.parseInt(statusAttr.toString()) : 500;

        HttpStatus httpStatus = HttpStatus.resolve(status);
        if (httpStatus == null) httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        String requestId = (String) request.getAttribute("requestId");
        if (requestId == null) requestId = UUID.randomUUID().toString();

        String code;
        String message;

        if (httpStatus == HttpStatus.NOT_FOUND) {
            code = "NOT_FOUND";
            message = "Endpoint does not exist";
        } else if (httpStatus == HttpStatus.METHOD_NOT_ALLOWED) {
            code = "METHOD_NOT_ALLOWED";
            message = "HTTP method not supported for this endpoint";
        } else {
            code = "ERROR";
            message = "Request failed";
        }

        ApiError error = ApiError.of(
                httpStatus.value(),
                code,
                message,
                requestId
        );

        return ResponseEntity.status(httpStatus).body(error);
    }
}
