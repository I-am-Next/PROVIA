package com.pm.paymentservicenew.application.event;

import com.pm.contracts.common.DomainEvent;

public interface DomainEventPublisher {

    void publish(

            DomainEvent event

    );

}