package com.pm.paymentservicenew.infrastructure.outbox;

import com.pm.paymentservicenew.integration.producer.KafkaTopicResolver;
import com.pm.paymentservicenew.integration.producer.PaymentEventProducer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    private OutboxEventRepository repository;

    @Mock
    private KafkaTopicResolver topicResolver;

    @Mock
    private PaymentEventProducer producer;

    @Mock
    private OutboxPublicationStateService publicationStateService;

    private OutboxPublisher outboxPublisher;

    @BeforeEach
    void setUp() {
        outboxPublisher = new OutboxPublisher(
                repository,
                topicResolver,
                producer,
                publicationStateService
        );
    }

    @AfterEach
    void clearInterruptFlag() {
        // Clear any interrupt left by the interruption test.
        Thread.interrupted();
    }

    @Test
    void shouldPublishPendingEventAndMarkItPublished()
            throws ExecutionException, InterruptedException, TimeoutException {

        OutboxEvent event = createEvent(OutboxStatus.PENDING);

        stubEvents(event);

        when(topicResolver.resolve(event.getEventType()))
                .thenReturn("payment.events");

        outboxPublisher.publishPendingEvents();

        verify(producer).publish(
                eq("payment.events"),
                eq(event.getAggregateId().toString()),
                eq(event.getPayload().getBytes(StandardCharsets.UTF_8))
        );

        verify(publicationStateService).markPublished(event.getId());

        verify(publicationStateService, never())
                .markFailed(any(UUID.class), any(Exception.class));
    }

    @Test
    void shouldRetryFailedEventAndMarkItPublished()
            throws ExecutionException, InterruptedException, TimeoutException {

        OutboxEvent event = createEvent(OutboxStatus.FAILED);

        stubEvents(event);

        when(topicResolver.resolve(event.getEventType()))
                .thenReturn("payment.events");

        outboxPublisher.publishPendingEvents();

        verify(producer).publish(
                eq("payment.events"),
                eq(event.getAggregateId().toString()),
                any(byte[].class)
        );

        verify(publicationStateService).markPublished(event.getId());

        verify(publicationStateService, never())
                .markFailed(any(UUID.class), any(Exception.class));
    }

    @Test
    void shouldMarkEventFailedWhenPublishingFails()
            throws ExecutionException, InterruptedException, TimeoutException {

        OutboxEvent event = createEvent(OutboxStatus.PENDING);

        RuntimeException exception =
                new RuntimeException("Kafka unavailable");

        stubEvents(event);

        when(topicResolver.resolve(event.getEventType()))
                .thenReturn("payment.events");

        doThrow(exception).when(producer).publish(
                eq("payment.events"),
                eq(event.getAggregateId().toString()),
                any(byte[].class)
        );

        outboxPublisher.publishPendingEvents();

        verify(publicationStateService).markFailed(
                event.getId(),
                exception
        );

        verify(publicationStateService, never())
                .markPublished(event.getId());
    }

    @Test
    void shouldMarkEventFailedWhenTopicResolutionFails() {

        OutboxEvent event = createEvent(OutboxStatus.PENDING);

        RuntimeException exception =
                new RuntimeException("Topic resolution failed");

        stubEvents(event);

        when(topicResolver.resolve(event.getEventType()))
                .thenThrow(exception);

        outboxPublisher.publishPendingEvents();

        verifyNoInteractions(producer);

        verify(publicationStateService).markFailed(
                event.getId(),
                exception
        );

        verify(publicationStateService, never())
                .markPublished(event.getId());
    }

    @Test
    void shouldDoNothingWhenThereAreNoEvents() {

        when(repository.findTop100ByStatusInOrderByOccurredAtAsc(
                List.of(OutboxStatus.PENDING, OutboxStatus.FAILED)
        )).thenReturn(List.of());

        outboxPublisher.publishPendingEvents();

        verify(repository).findTop100ByStatusInOrderByOccurredAtAsc(
                List.of(OutboxStatus.PENDING, OutboxStatus.FAILED)
        );

        verifyNoInteractions(
                topicResolver,
                producer,
                publicationStateService
        );
    }

    @Test
    void shouldContinuePublishingWhenOneEventFails()
            throws ExecutionException, InterruptedException, TimeoutException {

        OutboxEvent failedEvent = createEvent(OutboxStatus.PENDING);
        OutboxEvent successfulEvent = createEvent(OutboxStatus.PENDING);

        stubEvents(failedEvent, successfulEvent);

        when(topicResolver.resolve(failedEvent.getEventType()))
                .thenReturn("payment.events");

        when(topicResolver.resolve(successfulEvent.getEventType()))
                .thenReturn("payment.events");

        RuntimeException exception =
                new RuntimeException("Kafka unavailable");

        doThrow(exception).when(producer).publish(
                eq("payment.events"),
                eq(failedEvent.getAggregateId().toString()),
                any(byte[].class)
        );

        outboxPublisher.publishPendingEvents();

        verify(publicationStateService).markFailed(
                failedEvent.getId(),
                exception
        );

        verify(publicationStateService).markPublished(
                successfulEvent.getId()
        );

        verify(producer, times(2)).publish(
                eq("payment.events"),
                anyString(),
                any(byte[].class)
        );
    }

    @Test
    void shouldUseAggregateIdAsKafkaMessageKey()
            throws ExecutionException, InterruptedException, TimeoutException {

        OutboxEvent event = createEvent(OutboxStatus.PENDING);

        stubEvents(event);

        when(topicResolver.resolve(event.getEventType()))
                .thenReturn("payment.events");

        outboxPublisher.publishPendingEvents();

        ArgumentCaptor<String> keyCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(producer).publish(
                eq("payment.events"),
                keyCaptor.capture(),
                any(byte[].class)
        );

        assertEquals(
                event.getAggregateId().toString(),
                keyCaptor.getValue()
        );
    }

    @Test
    void shouldPublishPayloadAsUtf8Bytes()
            throws ExecutionException, InterruptedException, TimeoutException {

        String payload =
                "{\"paymentId\":\"123\",\"status\":\"COMPLETED\"}";

        OutboxEvent event = createEvent(
                OutboxStatus.PENDING,
                payload
        );

        stubEvents(event);

        when(topicResolver.resolve(event.getEventType()))
                .thenReturn("payment.events");

        outboxPublisher.publishPendingEvents();

        ArgumentCaptor<byte[]> payloadCaptor =
                ArgumentCaptor.forClass(byte[].class);

        verify(producer).publish(
                eq("payment.events"),
                eq(event.getAggregateId().toString()),
                payloadCaptor.capture()
        );

        assertArrayEquals(
                payload.getBytes(StandardCharsets.UTF_8),
                payloadCaptor.getValue()
        );
    }

    @Test
    void shouldAttemptToRecordFailureWhenMarkPublishedFails()
            throws ExecutionException, InterruptedException, TimeoutException {

        OutboxEvent event = createEvent(OutboxStatus.PENDING);

        stubEvents(event);

        when(topicResolver.resolve(event.getEventType()))
                .thenReturn("payment.events");

        doThrow(new RuntimeException("Database update failed"))
                .when(publicationStateService)
                .markPublished(event.getId());

        outboxPublisher.publishPendingEvents();

        verify(producer).publish(
                eq("payment.events"),
                eq(event.getAggregateId().toString()),
                any(byte[].class)
        );

        verify(publicationStateService).markPublished(event.getId());

        verify(publicationStateService).markFailed(
                eq(event.getId()),
                any(RuntimeException.class)
        );
    }

    @Test
    void shouldStopBatchWhenPublishingIsInterrupted()
            throws ExecutionException, InterruptedException, TimeoutException {

        OutboxEvent firstEvent = createEvent(OutboxStatus.PENDING);
        OutboxEvent secondEvent = createEvent(OutboxStatus.PENDING);

        stubEvents(firstEvent, secondEvent);

        when(topicResolver.resolve(firstEvent.getEventType()))
                .thenReturn("payment.events");

        InterruptedException exception =
                new InterruptedException("Interrupted while publishing");

        doThrow(exception).when(producer).publish(
                eq("payment.events"),
                eq(firstEvent.getAggregateId().toString()),
                any(byte[].class)
        );

        outboxPublisher.publishPendingEvents();

        assertTrue(Thread.currentThread().isInterrupted());

        verify(publicationStateService).markFailed(
                firstEvent.getId(),
                exception
        );

        verify(publicationStateService, never())
                .markPublished(firstEvent.getId());

        verify(producer, never()).publish(
                anyString(),
                eq(secondEvent.getAggregateId().toString()),
                any(byte[].class)
        );
    }

    private void stubEvents(OutboxEvent... events) {
        when(repository.findTop100ByStatusInOrderByOccurredAtAsc(
                List.of(OutboxStatus.PENDING, OutboxStatus.FAILED)
        )).thenReturn(List.of(events));
    }

    private OutboxEvent createEvent(OutboxStatus status) {
        return createEvent(status, "{\"paymentId\":\"123\"}");
    }

    private OutboxEvent createEvent(
            OutboxStatus status,
            String payload
    ) {
        OutboxEvent event = new OutboxEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "PAYMENT",
                OutboxEventType.PAYMENT_COMPLETED,
                payload,
                Instant.now()
        );

        if (status == OutboxStatus.FAILED) {
            event.markFailed(
                    new RuntimeException("Previous Kafka failure")
            );
        }

        return event;
    }
}
