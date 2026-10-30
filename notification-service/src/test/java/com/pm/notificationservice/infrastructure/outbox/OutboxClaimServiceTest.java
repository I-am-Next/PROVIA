package com.pm.notificationservice.infrastructure.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxClaimServiceTest {

    @Mock
    private OutboxEventRepository repository;

    private OutboxClaimService claimService;

    @BeforeEach
    void setUp() {
        claimService = new OutboxClaimService(repository, 300);
    }

    @Test
    void shouldReturnEmptyListWhenNoPendingEventsExist() {
        when(repository.claimPendingIds()).thenReturn(List.of());

        List<OutboxEvent> result = claimService.claimPendingBatch();

        assertTrue(result.isEmpty());
        verify(repository).recoverStaleClaims(any(Instant.class));
        verify(repository).claimPendingIds();
        verify(repository, never()).saveAll(any());
    }

    @Test
    void shouldClaimOnlyPendingEventsAndPersistProcessingStatus() {
        UUID eventId = UUID.randomUUID();

        OutboxEvent pendingEvent = OutboxEvent.create(
                UUID.randomUUID(),
                "BOOKING",
                "PAYMENT_COMPLETED",
                "{\"eventId\":\"" + eventId + "\"}",
                Instant.now()
        );

        when(repository.claimPendingIds())
                .thenReturn(List.of(pendingEvent.getId()));

        when(repository.findAllById(List.of(pendingEvent.getId())))
                .thenReturn(List.of(pendingEvent));

        when(repository.saveAll(any(Iterable.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<OutboxEvent> result = claimService.claimPendingBatch();

        assertEquals(1, result.size());
        assertEquals(OutboxStatus.PROCESSING, result.getFirst().getStatus());
        assertNotNull(result.getFirst().getClaimedAt());

        ArgumentCaptor<Iterable<OutboxEvent>> captor =
                ArgumentCaptor.forClass(Iterable.class);

        verify(repository).saveAll(captor.capture());

        Iterable<OutboxEvent> savedEvents = captor.getValue();
        assertTrue(savedEvents.iterator().hasNext());

        OutboxEvent savedEvent = savedEvents.iterator().next();
        assertEquals(OutboxStatus.PROCESSING, savedEvent.getStatus());

        verify(repository).recoverStaleClaims(any(Instant.class));
        verify(repository).claimPendingIds();
    }
}
