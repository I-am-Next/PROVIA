package com.pm.servicecatalog.infrastructure.outbox;

public enum OutboxStatus {

    PENDING,
    PROCESSING,
    PROCESSED,
    FAILED
}
