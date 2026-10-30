package com.pm.bookingservice.infrastructure.outbox;

public enum OutboxStatus {

    /**
     * Event has been persisted but not yet published.
     */
    PENDING,

    /**
     * Event has been successfully published to Kafka.
     */
    PUBLISHED,

    /**
     * Publishing failed.
     * The scheduler can retry this event later.
     */
    FAILED

}