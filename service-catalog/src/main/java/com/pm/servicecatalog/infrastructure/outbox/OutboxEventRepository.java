package com.pm.servicecatalog.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    @Query(value = """
        SELECT *
        FROM outbox_events
        WHERE status = 'PENDING'
          AND (
              next_retry_at IS NULL
              OR next_retry_at <= NOW()
          )
        ORDER BY created_at ASC
        LIMIT 50
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<OutboxEvent> fetchReadyEvents();

    @Modifying
    @Query(value = """
        UPDATE outbox_events
        SET status = 'PENDING',
            retry_count = retry_count + 1,
            processing_started_at = NULL,
            next_retry_at = NOW()
        WHERE status = 'PROCESSING'
          AND (
              processing_started_at IS NULL
              OR processing_started_at <=
                 NOW() - (:timeoutMinutes * INTERVAL '1 minute')
          )
        """, nativeQuery = true)
    int recoverStaleProcessingEvents(
            @Param("timeoutMinutes") int timeoutMinutes
    );
}
