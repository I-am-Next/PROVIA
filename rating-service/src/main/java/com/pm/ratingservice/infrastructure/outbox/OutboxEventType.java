package com.pm.ratingservice.infrastructure.outbox;

public enum OutboxEventType {

    CUSTOMER_RATED,
    CUSTOMER_RATING_UPDATED,
    CUSTOMER_RATING_DELETED,
    PROVIDER_RATED
}