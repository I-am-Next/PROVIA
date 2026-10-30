package com.pm.notificationservice.infrastructure.idempotency;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EventIdempotencyService {

    private final ProcessedEventRepository repository;

    public EventIdempotencyService(
            ProcessedEventRepository repository
    ) {
        this.repository = repository;
    }

    public boolean hasAlreadyProcessed(
            UUID eventId
    ) {

        return repository.existsByEventId(eventId);

    }

    @Transactional
    public void markProcessed(
            UUID eventId,
            String eventType
    ) {

        if (repository.existsByEventId(eventId)) {
            return;
        }

        repository.save(
                ProcessedEvent.create(
                        eventId,
                        eventType
                )
        );

    }

}