package com.pm.ratingservice.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByStatusOrderByOccurredAtAsc(
            OutboxStatus status
    );

    List<OutboxEvent> findTop100ByStatusInOrderByOccurredAtAsc(
            List<OutboxStatus> statuses
    );
}