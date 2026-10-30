package com.pm.ratingservice.domain.exception;

public class DuplicateRatingException
        extends RatingException {

    public DuplicateRatingException() {
        super("A rating already exists for this booking and reviewer");
    }
}