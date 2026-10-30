package com.pm.searchservice.infrastructure.consumer.catalog;

import com.pm.contracts.catalog.CategoryCreatedEvent;
import com.pm.contracts.catalog.CategoryDeletedEvent;
import com.pm.contracts.catalog.CategoryUpdatedEvent;
import com.pm.contracts.catalog.ServiceItemCreatedEvent;
import com.pm.contracts.catalog.ServiceItemDeletedEvent;
import com.pm.contracts.catalog.ServiceItemUpdatedEvent;
import com.pm.contracts.common.DomainEvent;
import org.springframework.stereotype.Component;

@Component
public class CatalogEventTypeResolver {

    public boolean supports(String eventType) {

        return switch (eventType) {

            case "CATEGORY_CREATED",
                 "CATEGORY_UPDATED",
                 "CATEGORY_DELETED",
                 "SERVICE_ITEM_CREATED",
                 "SERVICE_ITEM_UPDATED",
                 "SERVICE_ITEM_DELETED" -> true;

            default -> false;
        };
    }

    public Class<? extends DomainEvent> resolve(
            String eventType
    ) {

        return switch (eventType) {

            case "CATEGORY_CREATED" ->
                    CategoryCreatedEvent.class;

            case "CATEGORY_UPDATED" ->
                    CategoryUpdatedEvent.class;

            case "CATEGORY_DELETED" ->
                    CategoryDeletedEvent.class;

            case "SERVICE_ITEM_CREATED" ->
                    ServiceItemCreatedEvent.class;

            case "SERVICE_ITEM_UPDATED" ->
                    ServiceItemUpdatedEvent.class;

            case "SERVICE_ITEM_DELETED" ->
                    ServiceItemDeletedEvent.class;

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported catalog event type: "
                                    + eventType
                    );
        };
    }
}