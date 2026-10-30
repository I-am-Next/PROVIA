package com.pm.notificationservice.application.service;

import com.pm.contracts.common.DomainEvent;
import com.pm.notificationservice.application.service.impl.EventProcessingServiceImpl;
import com.pm.notificationservice.infrastructure.idempotency.EventIdempotencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;

class EventProcessingServiceImplTest {

    private EventIdempotencyService eventIdempotencyService;

    private EventProcessingServiceImpl service;

    private DomainEvent event;

    @BeforeEach
    void setUp() {

        eventIdempotencyService =
                mock(EventIdempotencyService.class);

        service =
                new EventProcessingServiceImpl(
                        eventIdempotencyService
                );

        event = mock(DomainEvent.class);

        when(event.eventId())
                .thenReturn(UUID.randomUUID());

        when(event.eventType())
                .thenReturn("CUSTOMER_RATED");
    }

    @Test
    void process_skipsAlreadyProcessedEvent() {

        when(eventIdempotencyService.hasAlreadyProcessed(
                event.eventId()
        )).thenReturn(true);

        Runnable action = mock(Runnable.class);

        service.process(
                event,
                action
        );

        verify(action, never()).run();

        verify(eventIdempotencyService)
                .hasAlreadyProcessed(event.eventId());

        verify(eventIdempotencyService, never())
                .markProcessed(
                        any(UUID.class),
                        anyString()
                );
    }

    @Test
    void process_executesActionAndMarksEventProcessed() {

        when(eventIdempotencyService.hasAlreadyProcessed(
                event.eventId()
        )).thenReturn(false);

        Runnable action = mock(Runnable.class);

        service.process(
                event,
                action
        );

        verify(action).run();

        verify(eventIdempotencyService)
                .markProcessed(
                        event.eventId(),
                        event.eventType()
                );
    }

    @Test
    void process_doesNotMarkEventProcessedWhenActionFails() {

        when(eventIdempotencyService.hasAlreadyProcessed(
                event.eventId()
        )).thenReturn(false);

        RuntimeException failure =
                new RuntimeException("notification creation failed");

        Runnable action = () -> {
            throw failure;
        };

        try {
            service.process(
                    event,
                    action
            );
        } catch (RuntimeException ignored) {
            // Expected.
        }

        verify(eventIdempotencyService, never())
                .markProcessed(
                        any(UUID.class),
                        anyString()
                );
    }
}