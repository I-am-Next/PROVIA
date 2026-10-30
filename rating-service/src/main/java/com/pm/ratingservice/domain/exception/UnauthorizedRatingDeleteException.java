package com.pm.ratingservice.domain.exception;

public class UnauthorizedRatingDeleteException extends RatingException {

    public UnauthorizedRatingDeleteException() {
        super("You are not authorized to delete this rating");
    }
}