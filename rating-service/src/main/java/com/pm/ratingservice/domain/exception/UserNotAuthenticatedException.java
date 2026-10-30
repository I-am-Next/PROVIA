package com.pm.ratingservice.domain.exception;

public class UserNotAuthenticatedException extends RatingException {

    public UserNotAuthenticatedException() {
        super("User is not authenticated");
    }
}