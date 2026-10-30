package com.pm.ratingservice.infrastructure.outbox;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "outbox_events",
        indexes = {
                @Index(
                        name = "idx_outbox_status_occurred_at",
                        columnList = "status, occurred_at"
                )
        }
)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private Instant occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    private Instant publishedAt;

    @Column(nullable = false)
    private int retryCount;

    @Column(length = 2000)
    private String lastError;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false)
    private String aggregateType;

    protected OutboxEvent() {
    }

    public OutboxEvent(
            String eventType,
            String payload,
            Instant occurredAt,
            UUID aggregateId,
            String aggregateType
    ) {
        this.eventType = eventType;
        this.payload = payload;
        this.occurredAt = occurredAt;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
    }

    public void markPublished() {

        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = Instant.now();
    }

    public void markFailed(String error) {

        this.status = OutboxStatus.FAILED;
        this.retryCount++;
        this.lastError = error;
    }

    public void markPending() {

        this.status = OutboxStatus.PENDING;
    }

    public UUID getId() {
        return id;
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

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getLastError() {
        return lastError;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }
}