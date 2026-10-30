package com.pm.notificationservice.infrastructure.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.common.DomainEvent;
import com.pm.notificationservice.application.service.EventProcessingService;
import com.pm.notificationservice.application.service.NotificationCreationService;

public abstract class AbstractEventHandler {

    protected final ObjectMapper objectMapper;

    protected final NotificationCreationService notificationCreationService;

    protected final EventProcessingService eventProcessingService;

    protected AbstractEventHandler(
            ObjectMapper objectMapper,
            NotificationCreationService notificationCreationService,
            EventProcessingService eventProcessingService
    ) {
        this.objectMapper = objectMapper;
        this.notificationCreationService = notificationCreationService;
        this.eventProcessingService = eventProcessingService;
    }

    protected <T> T convert(
            Object payload,
            Class<T> type
    ) {

        return objectMapper.convertValue(
                payload,
                type
        );

    }

    protected void process(
            DomainEvent event,
            Runnable action
    ) {

        eventProcessingService.process(
                event,
                action
        );

    }

}