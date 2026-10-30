package com.pm.ratingservice.application.port;

import com.pm.ratingservice.api.dto.response.BookingRatingContextResponse;

import java.util.UUID;

public interface BookingGateway {

    BookingRatingContextResponse getRatingContext(
            UUID bookingId
    );
}