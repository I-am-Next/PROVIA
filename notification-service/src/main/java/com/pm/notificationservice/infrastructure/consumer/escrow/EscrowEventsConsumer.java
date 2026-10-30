package com.pm.notificationservice.infrastructure.consumer.escrow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.common.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EscrowEventsConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    EscrowEventsConsumer.class
            );

    private final ObjectMapper objectMapper;

    private final EscrowEventHandler escrowEventHandler;

    private final EscrowEventTypeResolver eventTypeResolver;

    public EscrowEventsConsumer(
            ObjectMapper objectMapper,
            EscrowEventHandler escrowEventHandler,
            EscrowEventTypeResolver eventTypeResolver
    ) {

        this.objectMapper = objectMapper;
        this.escrowEventHandler = escrowEventHandler;
        this.eventTypeResolver = eventTypeResolver;
    }

    @KafkaListener(
            topics = "${notification.kafka.escrows}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            String message
    ) throws Exception {

        log.info(
                "========== ESCROW EVENT RECEIVED =========="
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
                    "Detected escrow event type={}, version={}",
                    type,
                    version
            );

            /*
             * Not every escrow event necessarily requires
             * a notification.
             */
            if (!eventTypeResolver.supports(type)) {

                log.info(
                        "Ignoring escrow event type={} because " +
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
                    "Escrow event deserialized successfully. " +
                            "type={}, version={}, payloadClass={}",
                    envelope.type(),
                    envelope.version(),
                    envelope.payload()
                            .getClass()
                            .getSimpleName()
            );

            log.info(
                    "Passing event to EscrowEventHandler..."
            );

            escrowEventHandler.handle(
                    envelope
            );

            log.info(
                    "Escrow event processing completed successfully. " +
                            "type={}, eventId={}",
                    type,
                    event.eventId()
            );

        } catch (Exception ex) {

            log.error(
                    "❌ FAILED PROCESSING ESCROW EVENT. Raw message={}",
                    message,
                    ex
            );

            throw ex;
        }
    }
}