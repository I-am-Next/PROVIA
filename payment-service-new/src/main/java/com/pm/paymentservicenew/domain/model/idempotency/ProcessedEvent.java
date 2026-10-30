package com.pm.paymentservicenew.domain.model.idempotency;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "processed_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_processed_event",
                        columnNames = {
                                "provider",
                                "idempotencyKey",
                                "eventType"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_processed_provider",
                        columnList = "provider"
                ),
                @Index(
                        name = "idx_processed_key",
                        columnList = "idempotencyKey"
                )
        }
)
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, updatable = false)
    private Instant processedAt;

    protected ProcessedEvent() {
    }

    private ProcessedEvent(
            String provider,
            String idempotencyKey,
            String eventType
    ) {

        this.provider = provider;
        this.idempotencyKey = idempotencyKey;
        this.eventType = eventType;
        this.processedAt = Instant.now();

    }

    public static ProcessedEvent create(
            String provider,
            String idempotencyKey,
            String eventType
    ) {

        return new ProcessedEvent(
                provider,
                idempotencyKey,
                eventType
        );

    }

    /*
     * Getters
     */

    public UUID getId() {
        return id;
    }

    public String getProvider() {
        return provider;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

}