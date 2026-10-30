package com.pm.paymentservicenew.infrastructure.outbox;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OutboxEventTest {

    @Test
    void shouldCreatePendingOutboxEvent() {

        UUID eventId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        Instant occurredAt = Instant.now();

        OutboxEvent event = new OutboxEvent(
                eventId,
                aggregateId,
                "PAYMENT",
                OutboxEventType.PAYMENT_COMPLETED,
                "{\"paymentId\":\"123\"}",
                occurredAt
        );

        assertEquals(
                eventId,
                event.getId()
        );

        assertEquals(
                aggregateId,
                event.getAggregateId()
        );

        assertEquals(
                "PAYMENT",
                event.getAggregateType()
        );

        assertEquals(
                OutboxEventType.PAYMENT_COMPLETED,
                event.getEventType()
        );

        assertEquals(
                "{\"paymentId\":\"123\"}",
                event.getPayload()
        );

        assertEquals(
                occurredAt,
                event.getOccurredAt()
        );

        assertEquals(
                OutboxStatus.PENDING,
                event.getStatus()
        );

        assertNull(
                event.getPublishedAt()
        );

        assertEquals(
                0,
                event.getRetryCount()
        );

        assertNull(
                event.getLastError()
        );
    }

    @Test
    void shouldMarkEventAsPublished() {

        OutboxEvent event = createEvent();

        RuntimeException previousFailure =
                new RuntimeException("Kafka unavailable");

        event.markFailed(previousFailure);

        assertEquals(
                OutboxStatus.FAILED,
                event.getStatus()
        );

        assertEquals(
                1,
                event.getRetryCount()
        );

        assertNotNull(
                event.getLastError()
        );

        event.markPublished();

        assertEquals(
                OutboxStatus.PUBLISHED,
                event.getStatus()
        );

        assertNotNull(
                event.getPublishedAt()
        );

        assertNull(
                event.getLastError()
        );

        assertEquals(
                1,
                event.getRetryCount()
        );
    }

    @Test
    void shouldMarkEventAsFailed() {

        OutboxEvent event = createEvent();

        RuntimeException exception =
                new RuntimeException("Kafka unavailable");

        event.markFailed(exception);

        assertEquals(
                OutboxStatus.FAILED,
                event.getStatus()
        );

        assertEquals(
                1,
                event.getRetryCount()
        );

        assertEquals(
                "Kafka unavailable",
                event.getLastError()
        );

        assertNull(
                event.getPublishedAt()
        );
    }

    @Test
    void shouldIncrementRetryCountWhenEventFailsAgain() {

        OutboxEvent event = createEvent();

        event.markFailed(
                new RuntimeException("First failure")
        );

        event.markFailed(
                new RuntimeException("Second failure")
        );

        assertEquals(
                OutboxStatus.FAILED,
                event.getStatus()
        );

        assertEquals(
                2,
                event.getRetryCount()
        );

        assertEquals(
                "Second failure",
                event.getLastError()
        );
    }

    @Test
    void shouldRetryFailedEvent() {

        OutboxEvent event = createEvent();

        event.markFailed(
                new RuntimeException("Kafka unavailable")
        );

        event.retry();

        assertEquals(
                OutboxStatus.PENDING,
                event.getStatus()
        );

        assertEquals(
                1,
                event.getRetryCount()
        );

        assertEquals(
                "Kafka unavailable",
                event.getLastError()
        );
    }

    private OutboxEvent createEvent() {

        return new OutboxEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "PAYMENT",
                OutboxEventType.PAYMENT_COMPLETED,
                "{\"paymentId\":\"123\"}",
                Instant.now()
        );
    }
}