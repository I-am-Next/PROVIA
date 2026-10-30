package com.pm.notificationservice.infrastructure.outbox;

public enum OutboxStatus {

    PENDING,

    PROCESSING,

    PUBLISHED,

    FAILED

}