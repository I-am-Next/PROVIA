package com.pm.searchservice.infrastructure.idempotency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class EventInboxService {

    private static final Logger log =
            LoggerFactory.getLogger(EventInboxService.class);

    private static final int PROCESSING_TIMEOUT_SECONDS = 120;

    private final InboxEventRepository repository;

    public EventInboxService(
            InboxEventRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public Optional<UUID> accept(
            UUID eventId,
            String topic,
            String eventType,
            int eventVersion,
            UUID aggregateId,
            String payload
    ) {
        int inserted = repository.insertIfAbsent(
                eventId,
                topic,
                eventType,
                eventVersion,
                aggregateId,
                payload
        );

        if (inserted == 1) {
            log.info(
                    "Event registered in inbox. topic={}, eventType={}, " +
                            "eventId={}, aggregateId={}",
                    topic,
                    eventType,
                    eventId,
                    aggregateId
            );
        } else {
            log.debug(
                    "Event already exists in inbox. topic={}, eventType={}, " +
                            "eventId={}, aggregateId={}",
                    topic,
                    eventType,
                    eventId,
                    aggregateId
            );
        }

        UUID processingToken = UUID.randomUUID();

        int claimed = repository.claim(
                eventId,
                processingToken,
                PROCESSING_TIMEOUT_SECONDS
        );

        if (claimed != 1) {
            log.warn(
                    "Inbox event claim unsuccessful. topic={}, eventType={}, " +
                            "eventId={}, aggregateId={}",
                    topic,
                    eventType,
                    eventId,
                    aggregateId
            );

            return Optional.empty();
        }

        log.info(
                "Inbox event claimed. topic={}, eventType={}, " +
                        "eventId={}, aggregateId={}",
                topic,
                eventType,
                eventId,
                aggregateId
        );

        return Optional.of(processingToken);
    }

    @Transactional
    public void markProcessed(
            UUID eventId,
            UUID processingToken
    ) {
        int updated = repository.markProcessed(
                eventId,
                processingToken
        );

        if (updated != 1) {
            log.error(
                    "Failed to mark inbox event as processed. eventId={}",
                    eventId
            );

            throw new IllegalStateException(
                    "Unable to mark inbox event as processed: "
                            + eventId
            );
        }

        log.info(
                "Inbox event marked PROCESSED. eventId={}",
                eventId
        );
    }

    @Transactional
    public void markPending(
            UUID eventId,
            UUID processingToken
    ) {
        int updated = repository.markPending(
                eventId,
                processingToken
        );

        if (updated != 1) {
            log.error(
                    "Failed to return inbox event to pending. eventId={}",
                    eventId
            );

            throw new IllegalStateException(
                    "Unable to return inbox event to pending: "
                            + eventId
            );
        }

        log.warn(
                "Inbox event returned to PENDING after processing failure. " +
                        "eventId={}",
                eventId
        );
    }

    @Transactional
    public void requeueStaleProcessing() {
        int recovered = repository.requeueStaleProcessing(
                PROCESSING_TIMEOUT_SECONDS
        );

        if (recovered > 0) {
            log.warn(
                    "Recovered stale inbox events. count={}, timeoutSeconds={}",
                    recovered,
                    PROCESSING_TIMEOUT_SECONDS
            );
        } else {
            log.debug(
                    "No stale inbox events required recovery."
            );
        }
    }

    @Transactional
    public Optional<UUID> claimForReplay(
            UUID eventId
    ) {
        UUID processingToken = UUID.randomUUID();

        int claimed = repository.claim(
                eventId,
                processingToken,
                PROCESSING_TIMEOUT_SECONDS
        );

        if (claimed != 1) {
            log.warn(
                    "Inbox event could not be claimed for replay. eventId={}",
                    eventId
            );

            return Optional.empty();
        }

        log.info(
                "Inbox event claimed for replay. eventId={}",
                eventId
        );

        return Optional.of(processingToken);
    }
}