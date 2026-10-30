package com.pm.authservice.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ValidationErrorResponse {

    private String code;
    private Map<String, String> fieldErrors;
    private LocalDateTime timestamp;

    public ValidationErrorResponse(String code, Map<String, String> fieldErrors, LocalDateTime timestamp) {
        this.code = code;
        this.fieldErrors = fieldErrors;
        this.timestamp = timestamp;
    }

    public String getCode() { return code; }
    public Map<String, String> getFieldErrors() { return fieldErrors; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
