package com.pm.authservice.exception;

public class EmailAlreadyExistsException extends ApiException {
    public EmailAlreadyExistsException() {
        super("AUTH_002", "Email already in use");
    }
}
