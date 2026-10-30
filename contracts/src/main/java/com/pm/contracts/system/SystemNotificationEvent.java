package com.pm.contracts.system;

import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.common.enums.RecipientRole;

import java.time.Instant;
import java.util.UUID;

public record SystemNotificationEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID recipientUserId,

        RecipientRole recipientRole,

        String title,

        String message,

        String actionUrl,

        String metadata

) implements DomainEvent {

    @Override
    public String eventType() {

        return "SYSTEM_NOTIFICATION";

    }

    @Override
    public UUID aggregateId() {

        return recipientUserId;

    }

    @Override
    public String aggregateType() {

        return "SYSTEM_NOTIFICATION";

    }

}