package com.pm.notificationservice.infrastructure.outbox;

import com.pm.contracts.common.DomainEvent;
import com.pm.notificationservice.application.port.DomainEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxDomainEventPublisher implements DomainEventPublisher {

    private final OutboxEventRepository outboxEventRepository;

    private final OutboxEventMapper outboxEventMapper;

    public OutboxDomainEventPublisher(
            OutboxEventRepository outboxEventRepository,
            OutboxEventMapper outboxEventMapper
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.outboxEventMapper = outboxEventMapper;
    }

    @Override
    @Transactional
    public void publish(
            DomainEvent event
    ) {

        OutboxEvent outboxEvent = outboxEventMapper.toOutboxEvent(
                event
        );

        outboxEventRepository.save(
                outboxEvent
        );

    }

}