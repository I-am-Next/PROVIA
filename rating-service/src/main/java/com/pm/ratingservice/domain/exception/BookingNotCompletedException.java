package com.pm.ratingservice.domain.exception;

public class BookingNotCompletedException
        extends RatingException {

    public BookingNotCompletedException() {
        super("Rating cannot be submitted because the booking has not been completed");
    }
}