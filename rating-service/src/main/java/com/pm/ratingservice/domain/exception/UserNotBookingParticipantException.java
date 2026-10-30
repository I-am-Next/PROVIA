package com.pm.ratingservice.domain.exception;

public class UserNotBookingParticipantException
        extends RatingException {

    public UserNotBookingParticipantException() {
        super("User is not a participant of this booking");
    }
}