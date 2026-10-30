package com.pm.providerservice.application.ports;

import com.pm.contracts.common.DomainEvent;

public interface OutboxDomainEventPublisher {

    void publish(DomainEvent event);
}