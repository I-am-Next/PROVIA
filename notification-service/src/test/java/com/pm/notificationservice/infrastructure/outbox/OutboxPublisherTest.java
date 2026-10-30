
package com.pm.notificationservice.infrastructure.outbox;

import com.pm.notificationservice.infrastructure.producer.KafkaTopicResolver;
import com.pm.notificationservice.infrastructure.producer.NotificationEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    private OutboxClaimService claimService;

    @Mock
    private NotificationEventProducer producer;

    @Mock
    private KafkaTopicResolver topicResolver;

    @Mock
    private OutboxStatusService statusService;

    private OutboxPublisher publisher;
    private OutboxEvent event;

    @BeforeEach
    void setUp() {
        publisher = new OutboxPublisher(
                claimService,
                producer,
                topicResolver,
                statusService
        );

        event = OutboxEvent.create(
                UUID.randomUUID(),
                "PAYMENT",
                "PAYMENT_COMPLETED",
                "{\"eventId\":\"" + UUID.randomUUID() + "\"}",
                Instant.now()
        );
    }

    @Test
    void successfulPublicationShouldUseAggregateIdAsKafkaKeyAndMarkPublished() {
        when(claimService.claimPendingBatch())
                .thenReturn(List.of(event));

        when(topicResolver.resolve(NotificationOutboxEventType.PAYMENT_COMPLETED))
                .thenReturn("payment-events");

        publisher.publishPendingEvents();

        verify(producer).publish(
                "payment-events",
                event.getAggregateId().toString(),
                event.getPayload()
        );

        verify(statusService).markPublished(event.getId());

        verify(statusService, never()).markFailed(any(), any());
    }

    @Test
    void failedPublicationShouldIncrementRetriesAndEventuallyFail() {
        OutboxEventRepository repository =
                mock(OutboxEventRepository.class);

        OutboxStatusService realStatusService =
                new OutboxStatusService(repository);

        when(repository.findById(event.getId()))
                .thenReturn(Optional.of(event));

        publisher = new OutboxPublisher(
                claimService,
                producer,
                topicResolver,
                realStatusService
        );

        when(topicResolver.resolve(NotificationOutboxEventType.PAYMENT_COMPLETED))
                .thenReturn("payment-events");

        doThrow(new IllegalStateException("Kafka unavailable"))
                .when(producer)
                .publish(
                        "payment-events",
                        event.getAggregateId().toString(),
                        event.getPayload()
                );

        for (int attempt = 1; attempt <= 5; attempt++) {
            when(claimService.claimPendingBatch())
                    .thenReturn(List.of(event));

            publisher.publishPendingEvents();

            assertEquals(attempt, event.getRetryCount());

            if (attempt < 5) {
                assertEquals(OutboxStatus.PENDING, event.getStatus());
            } else {
                assertEquals(OutboxStatus.FAILED, event.getStatus());
            }
        }

        verify(producer, times(5)).publish(
                "payment-events",
                event.getAggregateId().toString(),
                event.getPayload()
        );
    }
}
