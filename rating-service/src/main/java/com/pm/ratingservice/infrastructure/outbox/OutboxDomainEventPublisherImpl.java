package com.pm.ratingservice.infrastructure.outbox;

import com.pm.contracts.common.DomainEvent;
import com.pm.ratingservice.application.port.OutboxDomainEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class OutboxDomainEventPublisherImpl
        implements OutboxDomainEventPublisher {

    private final OutboxEventRepository repository;
    private final OutboxEventMapper mapper;

    public OutboxDomainEventPublisherImpl(
            OutboxEventRepository repository,
            OutboxEventMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void publish(DomainEvent event) {

        OutboxEvent outboxEvent =
                mapper.toOutboxEvent(event);

        repository.save(outboxEvent);
    }
}