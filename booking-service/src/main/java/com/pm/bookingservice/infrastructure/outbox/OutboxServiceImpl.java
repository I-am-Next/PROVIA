package com.pm.bookingservice.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.common.EventEnvelope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OutboxServiceImpl implements OutboxService {

    private final ObjectMapper objectMapper;

    private final OutboxRepository repository;

    public OutboxServiceImpl(
            ObjectMapper objectMapper,
            OutboxRepository repository
    ) {
        this.objectMapper = objectMapper;
        this.repository = repository;
    }

    @Override
    public void save(DomainEvent event) {

        try {

            EventEnvelope<DomainEvent> envelope =
                    new EventEnvelope<>(
                            event.eventType(),
                            event.eventVersion(),
                            event
                    );

            OutboxEvent outboxEvent =
                    new OutboxEvent(

                            event.eventId(),

                            event.aggregateId(),

                            event.aggregateType(),

                            event.eventType(),

                            event.eventVersion(),

                            objectMapper.writeValueAsBytes(
                                    envelope
                            ),

                            event.occurredAt()

                    );

            repository.save(outboxEvent);

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Unable to persist outbox event.",
                    ex
            );

        }

    }
}
