package com.pm.bookingservice.application.dto.response;

import java.util.UUID;

public record BookingRatingContextResponse(

        UUID bookingId,

        UUID customerId,

        UUID providerId,

        boolean completionConfirmed

) {
}