package com.pm.ratingservice.domain.exception;

public class UnauthorizedRatingUpdateException extends RatingException {

    public UnauthorizedRatingUpdateException() {
        super("You are not authorized to update this rating");
    }
}
