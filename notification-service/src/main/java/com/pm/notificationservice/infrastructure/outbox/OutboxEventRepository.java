
package com.pm.notificationservice.infrastructure.outbox;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    @Query(value = """
            SELECT id
            FROM outbox_events
            WHERE status = 'PENDING'
            ORDER BY occurred_at ASC
            LIMIT 100
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<UUID> claimPendingIds();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<OutboxEvent> findByStatusAndClaimedAtBefore(
            OutboxStatus status,
            Instant cutoff
    );

    @Modifying
    @Query(value = """
            UPDATE outbox_events
            SET status = 'PENDING',
                claimed_at = NULL
            WHERE status = 'PROCESSING'
              AND claimed_at < :cutoff
            """, nativeQuery = true)
    int recoverStaleClaims(@Param("cutoff") Instant cutoff);
}
