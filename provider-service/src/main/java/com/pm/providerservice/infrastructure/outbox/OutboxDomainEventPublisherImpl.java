package com.pm.providerservice.infrastructure.outbox;

import com.pm.contracts.common.DomainEvent;
import com.pm.providerservice.application.ports.OutboxDomainEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxDomainEventPublisherImpl
        implements OutboxDomainEventPublisher {

    private final OutboxEventRepository repository;
    private final OutboxEventMapper eventMapper;

    public OutboxDomainEventPublisherImpl(
            OutboxEventRepository repository,
            OutboxEventMapper eventMapper
    ) {
        this.repository = repository;
        this.eventMapper = eventMapper;
    }

    @Override
    @Transactional
    public void publish(
            DomainEvent event
    ) {

        String payload =
                eventMapper.toPayload(event);

        OutboxEvent outboxEvent =
                new OutboxEvent(
                        event.aggregateId(),
                        event.aggregateType(),
                        event.eventType(),
                        payload
                );

        repository.save(outboxEvent);
    }
}