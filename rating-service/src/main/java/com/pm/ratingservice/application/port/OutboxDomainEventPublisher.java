package com.pm.ratingservice.application.port;

import com.pm.contracts.common.DomainEvent;

public interface OutboxDomainEventPublisher {

    void publish(DomainEvent event);
}