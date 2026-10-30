package com.pm.bookingservice.application.dto.response.quote;

import com.pm.bookingservice.domain.model.quote.QuoteStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BookingQuoteResponse(

        UUID id,

        UUID bookingId,

        UUID providerId,

        BigDecimal labourAmount,

        BigDecimal materialsAmount,

        BigDecimal serviceFee,

        BigDecimal totalAmount,

        String description,

        Integer estimatedDurationDays,

        QuoteStatus status,

        Instant expiresAt,

        Instant createdAt

) {
}