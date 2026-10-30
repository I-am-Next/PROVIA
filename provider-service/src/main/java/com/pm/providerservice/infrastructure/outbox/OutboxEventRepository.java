package com.pm.providerservice.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    @Query("""
        SELECT o
        FROM OutboxEvent o
        WHERE o.status = com.pm.providerservice.infrastructure.outbox.OutboxStatus.PENDING
           OR (
                o.status = com.pm.providerservice.infrastructure.outbox.OutboxStatus.FAILED
                AND o.retryCount < 5
           )
        ORDER BY o.createdAt ASC
    """)
    List<OutboxEvent> findPendingEvents();
}