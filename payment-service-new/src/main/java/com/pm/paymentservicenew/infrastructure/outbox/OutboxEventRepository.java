package com.pm.paymentservicenew.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByStatusInOrderByOccurredAtAsc(
            Collection<OutboxStatus> statuses
    );
}