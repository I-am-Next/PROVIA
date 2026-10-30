package com.pm.ratingservice.domain.exception;

import java.util.UUID;

public class RatingNotFoundException extends RatingException {

    public RatingNotFoundException(UUID ratingId) {
        super("Rating not found: " + ratingId);
    }
}
