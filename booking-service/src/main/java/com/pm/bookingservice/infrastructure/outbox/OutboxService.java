package com.pm.bookingservice.infrastructure.outbox;

import com.pm.contracts.common.DomainEvent;

public interface OutboxService {

    void save(DomainEvent event);

}