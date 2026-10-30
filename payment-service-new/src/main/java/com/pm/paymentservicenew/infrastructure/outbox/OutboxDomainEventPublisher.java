package com.pm.paymentservicenew.infrastructure.outbox;

import com.pm.contracts.common.DomainEvent;
import com.pm.paymentservicenew.application.event.DomainEventPublisher;
import org.springframework.stereotype.Component;


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

    @Override
    public void publish(
            DomainEvent event
    ) {

        repository.save(

                mapper.toOutboxEvent(
                        event
                )

        );

    }

}