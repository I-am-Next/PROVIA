package com.pm.ratingservice.api.dto.response;

import java.util.UUID;

public record BookingRatingContextResponse(

        UUID bookingId,

        UUID customerId,

        UUID providerId,

        boolean completionConfirmed
) {
}