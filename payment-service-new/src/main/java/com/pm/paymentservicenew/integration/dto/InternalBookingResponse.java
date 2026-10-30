package com.pm.paymentservicenew.integration.dto;

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