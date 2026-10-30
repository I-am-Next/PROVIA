package com.pm.searchservice.infrastructure.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface InboxEventRepository
        extends JpaRepository<InboxEvent, UUID> {

    @Modifying
    @Query(
            value = """
                    INSERT INTO search_inbox_events (
                        event_id,
                        topic,
                        event_type,
                        event_version,
                        aggregate_id,
                        payload,
                        status,
                        received_at
                    )
                    VALUES (
                        :eventId,
                        :topic,
                        :eventType,
                        :eventVersion,
                        :aggregateId,
                        :payload,
                        'PENDING',
                        CURRENT_TIMESTAMP
                    )
                    ON CONFLICT (event_id) DO NOTHING
                    """,
            nativeQuery = true
    )
    int insertIfAbsent(
            @Param("eventId") UUID eventId,
            @Param("topic") String topic,
            @Param("eventType") String eventType,
            @Param("eventVersion") int eventVersion,
            @Param("aggregateId") UUID aggregateId,
            @Param("payload") String payload
    );

    @Modifying
    @Query(
            value = """
                    UPDATE search_inbox_events
                    SET
                        status = 'PROCESSING',
                        processing_started_at = CURRENT_TIMESTAMP,
                        processing_token = :processingToken
                    WHERE event_id = :eventId
                      AND (
                          status = 'PENDING'
                          OR (
                              status = 'PROCESSING'
                              AND processing_started_at <
                                  CURRENT_TIMESTAMP
                                  - (
                                      CAST(:staleSeconds AS INTEGER)
                                      * INTERVAL '1 second'
                                  )
                          )
                      )
                    """,
            nativeQuery = true
    )
    int claim(
            @Param("eventId") UUID eventId,
            @Param("processingToken") UUID processingToken,
            @Param("staleSeconds") int staleSeconds
    );

    @Modifying
    @Query(
            value = """
                    UPDATE search_inbox_events
                    SET
                        status = 'PROCESSED',
                        processed_at = CURRENT_TIMESTAMP,
                        processing_started_at = NULL,
                        processing_token = NULL
                    WHERE event_id = :eventId
                      AND status = 'PROCESSING'
                      AND processing_token = :processingToken
                    """,
            nativeQuery = true
    )
    int markProcessed(
            @Param("eventId") UUID eventId,
            @Param("processingToken") UUID processingToken
    );

    @Modifying
    @Query(
            value = """
                    UPDATE search_inbox_events
                    SET
                        status = 'PENDING',
                        processing_started_at = NULL,
                        processing_token = NULL
                    WHERE event_id = :eventId
                      AND status = 'PROCESSING'
                      AND processing_token = :processingToken
                    """,
            nativeQuery = true
    )
    int markPending(
            @Param("eventId") UUID eventId,
            @Param("processingToken") UUID processingToken
    );

    @Modifying
    @Query(
            value = """
                    UPDATE search_inbox_events
                    SET
                        status = 'PENDING',
                        processing_started_at = NULL,
                        processing_token = NULL
                    WHERE status = 'PROCESSING'
                      AND processing_started_at <
                          CURRENT_TIMESTAMP
                          - (
                              CAST(:staleSeconds AS INTEGER)
                              * INTERVAL '1 second'
                          )
                    """,
            nativeQuery = true
    )
    int requeueStaleProcessing(
            @Param("staleSeconds") int staleSeconds
    );

    List<InboxEvent>
    findTop100ByStatusOrderByReceivedAtAsc(
            InboxEventStatus status
    );
}