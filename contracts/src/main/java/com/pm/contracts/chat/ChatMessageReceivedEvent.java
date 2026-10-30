package com.pm.contracts.chat;

import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.common.enums.RecipientRole;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageReceivedEvent(

        UUID eventId,

        int eventVersion,

        Instant occurredAt,

        UUID conversationId,

        UUID messageId,

        UUID senderId,

        String senderName,

        UUID recipientId,

        RecipientRole recipientRole

) implements DomainEvent {

    @Override
    public String eventType() {

        return "CHAT_MESSAGE_RECEIVED";

    }

    @Override
    public UUID aggregateId() {

        return conversationId;

    }

    @Override
    public String aggregateType() {

        return "CHAT";

    }

}