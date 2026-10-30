package com.pm.userservice.exception;

public class UserProfileNotFoundException extends RuntimeException {

    public UserProfileNotFoundException() {
        super("User profile not found");
    }
}