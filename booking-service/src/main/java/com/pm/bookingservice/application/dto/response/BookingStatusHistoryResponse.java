package com.pm.bookingservice.application.dto.response;

import com.pm.bookingservice.domain.model.BookingStatus;

import java.time.Instant;
import java.util.UUID;

public record BookingStatusHistoryResponse(

        BookingStatus status,

        Instant changedAt,

        UUID changedBy,

        String note

) {
}