package com.pm.notificationservice.infrastructure.outbox;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OutboxStatusService {

    private final OutboxEventRepository repository;

    public OutboxStatusService(OutboxEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void markPublished(UUID eventId) {
        OutboxEvent event = repository.findById(eventId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Outbox event not found: " + eventId
                        )
                );

        if (event.getStatus() != OutboxStatus.PROCESSING) {
            return;
        }

        event.markPublished();
    }

    @Transactional
    public void markFailed(UUID eventId, String error) {
        OutboxEvent event = repository.findById(eventId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Outbox event not found: " + eventId
                        )
                );

        if (event.getStatus() != OutboxStatus.PROCESSING) {
            return;
        }

        event.markFailed(
                error == null || error.isBlank()
                        ? "Unknown Kafka publication failure"
                        : error
        );
    }
}
