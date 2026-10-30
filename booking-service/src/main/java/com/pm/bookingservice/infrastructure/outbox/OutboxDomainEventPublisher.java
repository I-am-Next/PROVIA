package com.pm.bookingservice.infrastructure.outbox;

import com.pm.bookingservice.application.event.DomainEventPublisher;
import com.pm.contracts.common.DomainEvent;
import org.springframework.stereotype.Component;

@Component
public class OutboxDomainEventPublisher
        implements DomainEventPublisher {

    private final OutboxService outboxService;

    public OutboxDomainEventPublisher(
            OutboxService outboxService
    ) {
        this.outboxService = outboxService;
    }

    @Override
    public void publish(
            DomainEvent event
    ) {

        outboxService.save(event);

    }

}