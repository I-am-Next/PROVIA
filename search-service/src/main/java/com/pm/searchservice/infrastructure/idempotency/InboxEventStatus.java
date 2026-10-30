package com.pm.searchservice.infrastructure.idempotency;

public enum InboxEventStatus {

    PENDING,

    PROCESSING,

    PROCESSED
}