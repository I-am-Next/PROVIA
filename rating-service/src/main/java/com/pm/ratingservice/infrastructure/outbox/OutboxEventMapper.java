package com.pm.ratingservice.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.common.EventEnvelope;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventMapper {

    private final ObjectMapper objectMapper;
    private final OutboxEventTypeResolver eventTypeResolver;

    public OutboxEventMapper(
            ObjectMapper objectMapper,
            OutboxEventTypeResolver eventTypeResolver
    ) {
        this.objectMapper = objectMapper;
        this.eventTypeResolver = eventTypeResolver;
    }

    public OutboxEvent toOutboxEvent(
            DomainEvent event
    ) {

        try {

            OutboxEventType eventType =
                    eventTypeResolver.resolve(event);

            EventEnvelope<DomainEvent> envelope =
                    new EventEnvelope<>(
                            eventType.name(),
                            event.eventVersion(),
                            event
                    );

            String payload =
                    objectMapper.writeValueAsString(
                            envelope
                    );

            return new OutboxEvent(
                    eventType.name(),
                    payload,
                    event.occurredAt(),
                    event.aggregateId(),
                    event.aggregateType()
            );

        } catch (JsonProcessingException ex) {

            throw new IllegalStateException(
                    "Failed to serialize rating domain event",
                    ex
            );
        }
    }
}