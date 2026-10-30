package com.pm.authservice.exception;

public class InvalidTokenException extends ApiException {
    public InvalidTokenException(String message) {
        super("AUTH_003", message);
    }
}
