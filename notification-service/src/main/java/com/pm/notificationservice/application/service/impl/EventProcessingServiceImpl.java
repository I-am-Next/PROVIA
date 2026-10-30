package com.pm.notificationservice.application.service.impl;

import com.pm.contracts.common.DomainEvent;
import com.pm.notificationservice.application.service.EventProcessingService;
import com.pm.notificationservice.infrastructure.idempotency.EventIdempotencyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventProcessingServiceImpl
        implements EventProcessingService {

    private final EventIdempotencyService eventIdempotencyService;

    public EventProcessingServiceImpl(
            EventIdempotencyService eventIdempotencyService
    ) {
        this.eventIdempotencyService = eventIdempotencyService;
    }

    @Override
    @Transactional
    public void process(
            DomainEvent event,
            Runnable action
    ) {

        if (eventIdempotencyService.hasAlreadyProcessed(
                event.eventId()
        )) {
            return;
        }

        action.run();

        eventIdempotencyService.markProcessed(
                event.eventId(),
                event.eventType()
        );

    }

}