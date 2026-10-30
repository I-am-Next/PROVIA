package com.pm.ratingservice.infrastructure.outbox;

import com.pm.ratingservice.integration.producer.RatingEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository repository;
    private final RatingEventProducer eventProducer;

    public OutboxPublisher(
            OutboxEventRepository repository,
            RatingEventProducer eventProducer
    ) {
        this.repository = repository;
        this.eventProducer = eventProducer;
    }

    @Scheduled(
            fixedDelayString = "${rating.outbox.publish-delay:5000}"
    )
    @Transactional
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                repository.findTop100ByStatusInOrderByOccurredAtAsc(
                        List.of(
                                OutboxStatus.PENDING,
                                OutboxStatus.FAILED
                        )
                );

        for (OutboxEvent event : events) {

            try {
                eventProducer.publish(event);

                event.markPublished();

                repository.save(event);

                log.info(
                        "Published rating outbox event. id={}, type={}, aggregateId={}",
                        event.getId(),
                        event.getEventType(),
                        event.getAggregateId()
                );

            } catch (Exception ex) {

                String errorMessage = ex.getMessage();

                if (errorMessage == null || errorMessage.isBlank()) {
                    errorMessage = ex.getClass().getSimpleName();
                }

                event.markFailed(errorMessage);

                repository.save(event);

                log.error(
                        "Failed to publish rating outbox event. "
                                + "id={}, type={}, retryCount={}",
                        event.getId(),
                        event.getEventType(),
                        event.getRetryCount(),
                        ex
                );
            }
        }
    }
}