
package com.pm.servicecatalog.infrastructure.outbox;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OutboxEventTransactionService {

    private static final int STALE_PROCESSING_TIMEOUT_MINUTES = 5;

    private final OutboxEventRepository repository;

    public OutboxEventTransactionService(
            OutboxEventRepository repository
    ) {
        this.repository = repository;
    }

    /**
     * Recovers abandoned claims independently of Kafka publishing.
     */
    @Scheduled(fixedDelayString =
            "${outbox.recovery-interval-ms:60000}")
    @Transactional
    public void recoverStaleProcessingEvents() {
        int recovered = repository.recoverStaleProcessingEvents(
                STALE_PROCESSING_TIMEOUT_MINUTES
        );

        if (recovered > 0) {
            System.out.println(
                    "Recovered " + recovered
                            + " stale outbox event(s)"
            );
        }
    }

    /**
     * Claims ready events in a short transaction.
     * Kafka publishing happens after this method returns.
     */
    @Transactional
    public List<OutboxEvent> claimReadyEvents() {
        List<OutboxEvent> events = repository.fetchReadyEvents();

        if (events.isEmpty()) {
            return List.of();
        }

        events.forEach(OutboxEvent::markProcessing);
        repository.saveAll(events);

        return events;
    }

    @Transactional
    public void markProcessed(UUID eventId) {
        OutboxEvent event = findEvent(eventId);
        event.markProcessed();
    }

    @Transactional
    public void scheduleRetry(UUID eventId) {
        OutboxEvent event = findEvent(eventId);
        event.scheduleRetry();
    }

    private OutboxEvent findEvent(UUID eventId) {
        return repository.findById(eventId)
                .orElseThrow(() -> new IllegalStateException(
                        "Outbox event not found: " + eventId
                ));
    }
}
