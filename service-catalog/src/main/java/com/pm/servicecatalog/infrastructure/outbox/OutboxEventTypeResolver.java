package com.pm.servicecatalog.infrastructure.outbox;

import com.pm.contracts.catalog.CategoryCreatedEvent;
import com.pm.contracts.catalog.CategoryDeletedEvent;
import com.pm.contracts.catalog.CategoryUpdatedEvent;
import com.pm.contracts.catalog.ServiceItemCreatedEvent;
import com.pm.contracts.catalog.ServiceItemDeletedEvent;
import com.pm.contracts.catalog.ServiceItemUpdatedEvent;
import com.pm.contracts.common.DomainEvent;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventTypeResolver {

    public OutboxEventType resolve(
            DomainEvent event
    ) {

        if (event instanceof CategoryCreatedEvent) {
            return OutboxEventType.CATEGORY_CREATED;
        }

        if (event instanceof CategoryUpdatedEvent) {
            return OutboxEventType.CATEGORY_UPDATED;
        }

        if (event instanceof CategoryDeletedEvent) {
            return OutboxEventType.CATEGORY_DELETED;
        }

        if (event instanceof ServiceItemCreatedEvent) {
            return OutboxEventType.SERVICE_ITEM_CREATED;
        }

        if (event instanceof ServiceItemUpdatedEvent) {
            return OutboxEventType.SERVICE_ITEM_UPDATED;
        }

        if (event instanceof ServiceItemDeletedEvent) {
            return OutboxEventType.SERVICE_ITEM_DELETED;
        }

        throw new IllegalArgumentException(
                "Unsupported catalog domain event: "
                        + event.getClass().getName()
        );
    }
}