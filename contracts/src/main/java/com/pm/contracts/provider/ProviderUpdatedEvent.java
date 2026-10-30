package com.pm.contracts.provider;

import com.pm.contracts.common.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ProviderUpdatedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID providerId,

        String authUserId,

        String businessName,

        String email,

        String category,

        String description,

        String phone,

        boolean verified,

        UUID profilePhotoMediaId

) implements DomainEvent {

    @Override
    public String eventType() {

        return "PROVIDER_UPDATED";
    }

    @Override
    public UUID aggregateId() {

        return providerId;
    }

    @Override
    public String aggregateType() {

        return "PROVIDER";
    }
}