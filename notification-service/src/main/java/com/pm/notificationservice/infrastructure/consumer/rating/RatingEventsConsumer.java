package com.pm.notificationservice.infrastructure.consumer.rating;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.common.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RatingEventsConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    RatingEventsConsumer.class
            );

    private final ObjectMapper objectMapper;
    private final RatingEventHandler ratingEventHandler;
    private final RatingEventTypeResolver eventTypeResolver;

    public RatingEventsConsumer(
            ObjectMapper objectMapper,
            RatingEventHandler ratingEventHandler,
            RatingEventTypeResolver eventTypeResolver
    ) {

        this.objectMapper = objectMapper;
        this.ratingEventHandler = ratingEventHandler;
        this.eventTypeResolver = eventTypeResolver;
    }

    @KafkaListener(
            topics = "${notification.kafka.ratings}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            String message
    ) throws Exception {

        log.info(
                "========== RATING EVENT RECEIVED =========="
        );

        log.info(
                "Raw Kafka message: {}",
                message
        );

        try {

            JsonNode root =
                    objectMapper.readTree(message);

            String type =
                    root.get("type").asText();

            int version =
                    root.get("version").asInt();

            JsonNode payloadNode =
                    root.get("payload");

            log.info(
                    "Detected rating event type={}, version={}",
                    type,
                    version
            );

            /*
             * Ignore rating events that Notification Service
             * does not currently handle.
             */
            if (!eventTypeResolver.supports(type)) {

                log.info(
                        "Ignoring rating event type={} because " +
                                "Notification Service does not handle this event.",
                        type
                );

                return;
            }

            Class<? extends DomainEvent> eventClass =
                    eventTypeResolver.resolve(type);

            DomainEvent event =
                    objectMapper.treeToValue(
                            payloadNode,
                            eventClass
                    );

            EventEnvelope<DomainEvent> envelope =
                    new EventEnvelope<>(
                            type,
                            version,
                            event
                    );

            log.info(
                    "Rating event deserialized successfully. " +
                            "type={}, version={}, payloadClass={}",
                    envelope.type(),
                    envelope.version(),
                    envelope.payload()
                            .getClass()
                            .getSimpleName()
            );

            log.info(
                    "Passing event to RatingEventHandler..."
            );

            ratingEventHandler.handle(
                    envelope
            );

            log.info(
                    "Rating event processing completed successfully. " +
                            "type={}, eventId={}",
                    type,
                    event.eventId()
            );

        } catch (Exception ex) {

            log.error(
                    "❌ FAILED PROCESSING RATING EVENT. Raw message={}",
                    message,
                    ex
            );

            throw ex;
        }
    }
}