package com.pm.notificationservice.infrastructure.consumer.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.common.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventsConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    PaymentEventsConsumer.class
            );

    private final ObjectMapper objectMapper;

    private final PaymentEventHandler paymentEventHandler;

    private final PaymentEventTypeResolver eventTypeResolver;

    public PaymentEventsConsumer(
            ObjectMapper objectMapper,
            PaymentEventHandler paymentEventHandler,
            PaymentEventTypeResolver eventTypeResolver
    ) {

        this.objectMapper = objectMapper;
        this.paymentEventHandler = paymentEventHandler;
        this.eventTypeResolver = eventTypeResolver;
    }

    @KafkaListener(
            topics = "${notification.kafka.payments}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            String message
    ) throws Exception {

        log.info(
                "========== PAYMENT EVENT RECEIVED =========="
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
                    "Detected payment event type={}, version={}",
                    type,
                    version
            );

            /*
             * Ignore payment events that Notification Service
             * does not currently handle.
             */
            if (!eventTypeResolver.supports(type)) {

                log.info(
                        "Ignoring payment event type={} because " +
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
                    "Payment event deserialized successfully. " +
                            "type={}, version={}, payloadClass={}",
                    envelope.type(),
                    envelope.version(),
                    envelope.payload()
                            .getClass()
                            .getSimpleName()
            );

            log.info(
                    "Passing event to PaymentEventHandler..."
            );

            paymentEventHandler.handle(
                    envelope
            );

            log.info(
                    "Payment event processing completed successfully. " +
                            "type={}, eventId={}",
                    type,
                    event.eventId()
            );

        } catch (Exception ex) {

            log.error(
                    "❌ FAILED PROCESSING PAYMENT EVENT. Raw message={}",
                    message,
                    ex
            );

            throw ex;
        }
    }
}