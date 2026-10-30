package com.pm.ratingservice.infrastructure.outbox;

public enum OutboxStatus {

    PENDING,
    PUBLISHED,
    FAILED
}