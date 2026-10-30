package com.pm.notificationservice.infrastructure.consumer.quote;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.common.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class QuoteEventsConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    QuoteEventsConsumer.class
            );

    private final ObjectMapper objectMapper;

    private final QuoteEventHandler quoteEventHandler;

    private final QuoteEventResolver quoteEventResolver;

    public QuoteEventsConsumer(
            ObjectMapper objectMapper,
            QuoteEventHandler quoteEventHandler,
            QuoteEventResolver quoteEventResolver
    ) {

        this.objectMapper = objectMapper;
        this.quoteEventHandler = quoteEventHandler;
        this.quoteEventResolver = quoteEventResolver;
    }

    @KafkaListener(
            topics = "${notification.kafka.quotes}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            String message
    ) throws Exception {

        log.info(
                "========== QUOTE EVENT RECEIVED =========="
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
                    "Detected quote event type={}, version={}",
                    type,
                    version
            );

            /*
             * Notification Service does not necessarily
             * handle every quote-related event.
             *
             * Unsupported events are ignored rather than
             * causing the Kafka consumer to fail.
             */
            if (!quoteEventResolver.supports(type)) {

                log.info(
                        "Ignoring quote event type={} because Notification Service does not handle this event.",
                        type
                );

                return;
            }

            Class<? extends DomainEvent> eventClass =
                    quoteEventResolver.resolve(type);

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
                    "Quote event deserialized successfully. " +
                            "type={}, version={}, payloadClass={}",
                    envelope.type(),
                    envelope.version(),
                    envelope.payload()
                            .getClass()
                            .getSimpleName()
            );

            log.info(
                    "Passing quote event to QuoteEventHandler..."
            );

            quoteEventHandler.handle(
                    envelope
            );

            log.info(
                    "Quote event processing completed. type={}, eventId={}",
                    type,
                    event.eventId()
            );

        } catch (Exception ex) {

            log.error(
                    "❌ FAILED PROCESSING QUOTE EVENT. Raw message={}",
                    message,
                    ex
            );

            throw ex;
        }
    }
}