
package com.pm.servicecatalog.infrastructure.outbox;

import com.pm.contracts.common.DomainEvent;
import com.pm.servicecatalog.application.event.DomainEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxDomainEventPublisher
        implements DomainEventPublisher {

    private final OutboxEventRepository repository;
    private final OutboxEventMapper mapper;

    public OutboxDomainEventPublisher(
            OutboxEventRepository repository,
            OutboxEventMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * An outbox event must be saved inside the same transaction
     * as the domain change that produced it.
     *
     * MANDATORY prevents this method from being called without
     * an existing transaction. It joins the caller's transaction
     * instead of starting an independent one.
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "Domain event must not be null."
            );
        }

        OutboxEvent outboxEvent = mapper.toOutboxEvent(event);

        repository.save(outboxEvent);
    }
}
