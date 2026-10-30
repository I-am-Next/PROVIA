package com.pm.searchservice.infrastructure.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "search_inbox_events")
public class InboxEvent {

    @Id
    @Column(
            name = "event_id",
            nullable = false,
            updatable = false
    )
    private UUID eventId;

    @Column(
            name = "topic",
            nullable = false,
            length = 255
    )
    private String topic;

    @Column(
            name = "event_type",
            nullable = false,
            length = 255
    )
    private String eventType;

    @Column(
            name = "event_version",
            nullable = false
    )
    private int eventVersion;

    @Column(
            name = "aggregate_id"
    )
    private UUID aggregateId;

    @Column(
            name = "payload",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 32
    )
    private InboxEventStatus status;

    @Column(
            name = "received_at",
            nullable = false
    )
    private Instant receivedAt;

    @Column(
            name = "processed_at"
    )
    private Instant processedAt;

    protected InboxEvent() {
    }

    public InboxEvent(
            UUID eventId,
            String topic,
            String eventType,
            int eventVersion,
            UUID aggregateId,
            String payload
    ) {
        this.eventId = eventId;
        this.topic = topic;
        this.eventType = eventType;
        this.eventVersion = eventVersion;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.status = InboxEventStatus.PENDING;
        this.receivedAt = Instant.now();
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getTopic() {
        return topic;
    }

    public String getEventType() {
        return eventType;
    }

    public int getEventVersion() {
        return eventVersion;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getPayload() {
        return payload;
    }

    public InboxEventStatus getStatus() {
        return status;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void markProcessing() {
        this.status = InboxEventStatus.PROCESSING;
    }

    public void markPending() {
        this.status = InboxEventStatus.PENDING;
    }

    public void markProcessed() {
        this.status = InboxEventStatus.PROCESSED;
        this.processedAt = Instant.now();
    }
}