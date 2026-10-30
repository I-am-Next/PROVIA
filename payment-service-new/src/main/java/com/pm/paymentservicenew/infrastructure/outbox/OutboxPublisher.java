
package com.pm.paymentservicenew.infrastructure.outbox;

import com.pm.paymentservicenew.integration.producer.KafkaTopicResolver;
import com.pm.paymentservicenew.integration.producer.PaymentEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class OutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository repository;
    private final KafkaTopicResolver topicResolver;
    private final PaymentEventProducer producer;
    private final OutboxPublicationStateService publicationStateService;

    public OutboxPublisher(
            OutboxEventRepository repository,
            KafkaTopicResolver topicResolver,
            PaymentEventProducer producer,
            OutboxPublicationStateService publicationStateService
    ) {
        this.repository = repository;
        this.topicResolver = topicResolver;
        this.producer = producer;
        this.publicationStateService = publicationStateService;
    }

    @Scheduled(
            fixedDelayString = "${payment.outbox.publish-delay:5000}"
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
            try {
                producer.publish(
                        topicResolver.resolve(event.getEventType()),
                        event.getAggregateId().toString(),
                        event.getPayload().getBytes(StandardCharsets.UTF_8)
                );

                publicationStateService.markPublished(event.getId());

                log.info(
                        "Published outbox event {} [{}]",
                        event.getId(),
                        event.getEventType()
                );

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();

                try {
                    publicationStateService.markFailed(event.getId(), ex);
                } catch (Exception stateException) {
                    log.error(
                            "Could not record interrupted publication for event {}",
                            event.getId(),
                            stateException
                    );
                }

                // Preserve the interrupt and stop processing this batch.
                return;

            } catch (Exception ex) {
                log.error(
                        "Failed to publish outbox event {} [{}]",
                        event.getId(),
                        event.getEventType(),
                        ex
                );

                try {
                    publicationStateService.markFailed(event.getId(), ex);
                } catch (Exception stateException) {
                    log.error(
                            "Could not record failed publication for event {}",
                            event.getId(),
                            stateException
                    );
                }
            }
        }
    }
}
