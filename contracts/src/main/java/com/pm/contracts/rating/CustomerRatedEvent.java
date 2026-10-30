package com.pm.contracts.rating;

import com.pm.contracts.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record CustomerRatedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID bookingId,

        UUID ratingId,

        UUID providerId,

        UUID customerId,

        Integer rating,

        Double averageRating,

        Long totalRatings

) implements DomainEvent {

    @Override
    public String eventType() {

        return "CUSTOMER_RATED";
    }

    @Override
    public UUID aggregateId() {

        return ratingId;
    }

    @Override
    public String aggregateType() {

        return "RATING";
    }
}