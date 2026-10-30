
package com.pm.bookingservice.infrastructure.outbox;

import com.pm.bookingservice.integration.producer.BookingEventProducer;
import com.pm.bookingservice.integration.producer.BookingTopicResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Component
public class OutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxRepository repository;
    private final BookingEventProducer producer;
    private final BookingTopicResolver topicResolver;
    private final OutboxPublicationStateService publicationStateService;

    public OutboxPublisher(
            OutboxRepository repository,
            BookingEventProducer producer,
            BookingTopicResolver topicResolver,
            OutboxPublicationStateService publicationStateService
    ) {
        this.repository = repository;
        this.producer = producer;
        this.topicResolver = topicResolver;
        this.publicationStateService = publicationStateService;
    }

    @Scheduled(
            fixedDelayString = "${booking.outbox.publish-delay:5000}"
    )
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                repository.findTop100ByStatusInOrderByOccurredAtAsc(
                        List.of(
                                OutboxStatus.PENDING,
                                OutboxStatus.FAILED
                        )
                );

        for (OutboxEvent event : events) {
            publishOne(event);
        }
    }

    private void publishOne(OutboxEvent event) {

        try {
            String topic = topicResolver.resolve(
                    event.getAggregateType()
            );

            producer.publish(
                    topic,
                    event.getAggregateId().toString(),
                    event.getPayload()
            );

            publicationStateService.markPublished(event.getId());

            log.info(
                    "Published outbox event. id={}, type={}, topic={}, "
                            + "aggregateId={}",
                    event.getId(),
                    event.getEventType(),
                    topic,
                    event.getAggregateId()
            );

        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            recordFailure(event, ex);

            throw new IllegalStateException(
                    "Outbox publishing interrupted for event "
                            + event.getId(),
                    ex
            );

        } catch (ExecutionException | TimeoutException | RuntimeException ex) {
            recordFailure(event, ex);

        }
    }

    private void recordFailure(
            OutboxEvent event,
            Exception exception
    ) {
        log.error(
                "Outbox publication failed. id={}, type={}, retryCount={}",
                event.getId(),
                event.getEventType(),
                event.getRetryCount(),
                exception
        );

        try {
            publicationStateService.markFailed(
                    event.getId(),
                    exception
            );
        } catch (RuntimeException stateException) {
            stateException.addSuppressed(exception);

            throw new IllegalStateException(
                    "Unable to persist publication failure for event "
                            + event.getId(),
                    stateException
            );
        }
    }
}
