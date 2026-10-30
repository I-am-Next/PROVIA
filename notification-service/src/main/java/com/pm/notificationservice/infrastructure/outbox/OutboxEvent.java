
package com.pm.notificationservice.infrastructure.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private Instant occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(length = 4000)
    private String lastError;

    private Instant publishedAt;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    protected OutboxEvent() {
    }

    private OutboxEvent(
            UUID id,
            UUID aggregateId,
            String aggregateType,
            String eventType,
            String payload,
            Instant occurredAt,
            OutboxStatus status,
            int retryCount,
            String lastError,
            Instant publishedAt,
            Instant claimedAt
    ) {
        this.id = id;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = payload;
        this.occurredAt = occurredAt;
        this.status = status;
        this.retryCount = retryCount;
        this.lastError = lastError;
        this.publishedAt = publishedAt;
        this.claimedAt = claimedAt;
    }

    public static OutboxEvent create(
            UUID aggregateId,
            String aggregateType,
            String eventType,
            String payload,
            Instant occurredAt
    ) {
        return new OutboxEvent(
                UUID.randomUUID(),
                aggregateId,
                aggregateType,
                eventType,
                payload,
                occurredAt,
                OutboxStatus.PENDING,
                0,
                null,
                null,
                null
        );
    }

    public void markProcessing(Instant now) {
        this.status = OutboxStatus.PROCESSING;
        this.claimedAt = now;
    }

    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.lastError = null;
        this.claimedAt = null;
    }

    public void markFailed(String error) {
        this.retryCount++;
        this.lastError = error;
        this.claimedAt = null;

        if (this.retryCount >= 5) {
            this.status = OutboxStatus.FAILED;
        } else {
            this.status = OutboxStatus.PENDING;
        }
    }

    public void recoverStaleClaim() {
        if (this.status == OutboxStatus.PROCESSING) {
            this.status = OutboxStatus.PENDING;
            this.claimedAt = null;
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getClaimedAt() {
        return claimedAt;
    }
}
