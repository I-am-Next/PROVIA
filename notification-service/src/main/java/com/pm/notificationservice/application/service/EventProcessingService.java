package com.pm.notificationservice.application.service;

import com.pm.contracts.common.DomainEvent;

public interface EventProcessingService {

    void process(
            DomainEvent event,
            Runnable action
    );

}