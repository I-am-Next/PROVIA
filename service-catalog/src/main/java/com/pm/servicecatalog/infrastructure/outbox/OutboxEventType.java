package com.pm.servicecatalog.infrastructure.outbox;

public enum OutboxEventType {

    CATEGORY_CREATED,
    CATEGORY_UPDATED,
    CATEGORY_DELETED,

    SERVICE_ITEM_CREATED,
    SERVICE_ITEM_UPDATED,
    SERVICE_ITEM_DELETED

}