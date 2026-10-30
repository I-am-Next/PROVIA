package com.pm.notificationservice.application.port;

import com.pm.contracts.common.DomainEvent;

public interface DomainEventPublisher {

    void publish(DomainEvent event);

}