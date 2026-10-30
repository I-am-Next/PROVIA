package com.pm.notificationservice.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.common.EventEnvelope;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventMapper {

    private final ObjectMapper objectMapper;

    public OutboxEventMapper(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    public OutboxEvent toOutboxEvent(
            DomainEvent event
    ) {

        try {

            EventEnvelope<DomainEvent> envelope =
                    new EventEnvelope<>(

                            event.eventType(),

                            event.eventVersion(),

                            event

                    );

            return OutboxEvent.create(

                    event.aggregateId(),

                    event.aggregateType(),

                    event.eventType(),

                    objectMapper.writeValueAsString(envelope),

                    event.occurredAt()

            );

        } catch (JsonProcessingException ex) {

            throw new IllegalStateException(

                    "Failed to serialize event envelope.",

                    ex

            );

        }

    }

}