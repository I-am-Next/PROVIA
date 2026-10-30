package com.pm.providerservice.infrastructure.outbox;

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

    public String toPayload(
            DomainEvent event
    ) {

        try {

            EventEnvelope<DomainEvent> envelope =
                    new EventEnvelope<>(
                            event.eventType(),
                            event.eventVersion(),
                            event
                    );

            return objectMapper.writeValueAsString(
                    envelope
            );

        } catch (JsonProcessingException ex) {

            throw new IllegalStateException(
                    "Failed to serialize provider event: "
                            + event.eventType(),
                    ex
            );
        }
    }
}