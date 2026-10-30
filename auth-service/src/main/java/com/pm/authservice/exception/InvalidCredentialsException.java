package com.pm.authservice.exception;

public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super("AUTH_001", "Invalid email or password");
    }
}
