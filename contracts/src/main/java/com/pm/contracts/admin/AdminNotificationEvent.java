package com.pm.contracts.admin;

import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.common.enums.RecipientRole;

import java.time.Instant;
import java.util.UUID;

public record AdminNotificationEvent(

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

        return "ADMIN_NOTIFICATION";

    }

    @Override
    public UUID aggregateId() {

        return recipientUserId;

    }

    @Override
    public String aggregateType() {

        return "ADMIN_NOTIFICATION";

    }

}