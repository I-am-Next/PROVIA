package com.pm.bookingservice.application.dto.response.internal;

import com.pm.bookingservice.domain.model.BookingStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record InternalBookingResponse(

        UUID bookingId,

        UUID customerId,

        UUID providerId,

        BigDecimal amount,

        BookingStatus status

) {
}