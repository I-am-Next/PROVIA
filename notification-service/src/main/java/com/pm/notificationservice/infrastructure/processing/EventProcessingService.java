package com.pm.notificationservice.infrastructure.processing;

import com.pm.contracts.common.DomainEvent;
import com.pm.notificationservice.infrastructure.idempotency.EventIdempotencyService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EventProcessingService {

    private final EventIdempotencyService eventIdempotencyService;

    public EventProcessingService(
            EventIdempotencyService eventIdempotencyService
    ) {
        this.eventIdempotencyService = eventIdempotencyService;
    }

    @Transactional
    public void process(
            DomainEvent event,
            Runnable action
    ) {

        if (eventIdempotencyService.hasAlreadyProcessed(event.eventId())) {
            return;
        }

        action.run();

        eventIdempotencyService.markProcessed(
                event.eventId(),
                event.eventType()
        );

    }

}