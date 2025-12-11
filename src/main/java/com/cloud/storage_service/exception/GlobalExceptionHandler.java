package com.cloud.storage_service.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebException.class)
    public ResponseEntity<String> handleWebException(WebException ex) {
        return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
    }
}
