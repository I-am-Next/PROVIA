package com.pm.paymentservicenew.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.common.EventEnvelope;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventMapper {

    private final ObjectMapper objectMapper;

    private final OutboxEventTypeResolver resolver;

    public OutboxEventMapper(
            ObjectMapper objectMapper,
            OutboxEventTypeResolver resolver
    ) {

        this.objectMapper = objectMapper;
        this.resolver = resolver;

    }

    public OutboxEvent toOutboxEvent(
            DomainEvent event
    ) {

        try {

            OutboxEventType eventType =
                    resolver.resolve(event);

            EventEnvelope<DomainEvent> envelope =
                    new EventEnvelope<>(
                            event.eventType(),
                            event.eventVersion(),
                            event
                    );

            String payload =
                    objectMapper.writeValueAsString(
                            envelope
                    );

            return new OutboxEvent(

                    event.eventId(),

                    event.aggregateId(),

                    event.aggregateType(),

                    eventType,

                    payload,

                    event.occurredAt()

            );

        } catch (JsonProcessingException ex) {

            throw new IllegalStateException(
                    "Unable to serialize domain event.",
                    ex
            );

        }

    }

}