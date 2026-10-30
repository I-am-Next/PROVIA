package com.pm.notificationservice.infrastructure.consumer.wallet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.common.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class WalletEventsConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    WalletEventsConsumer.class
            );

    private final ObjectMapper objectMapper;

    private final WalletEventHandler walletEventHandler;

    private final WalletEventTypeResolver eventTypeResolver;

    public WalletEventsConsumer(
            ObjectMapper objectMapper,
            WalletEventHandler walletEventHandler,
            WalletEventTypeResolver eventTypeResolver
    ) {

        this.objectMapper = objectMapper;
        this.walletEventHandler = walletEventHandler;
        this.eventTypeResolver = eventTypeResolver;
    }

    @KafkaListener(
            topics = "${notification.kafka.wallets}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            String message
    ) throws Exception {

        log.info(
                "========== WALLET EVENT RECEIVED =========="
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
                    "Detected wallet event type={}, version={}",
                    type,
                    version
            );

            /*
             * Ignore wallet events that Notification Service
             * does not currently handle.
             */
            if (!eventTypeResolver.supports(type)) {

                log.info(
                        "Ignoring wallet event type={} because " +
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
                    "Wallet event deserialized successfully. " +
                            "type={}, version={}, payloadClass={}",
                    envelope.type(),
                    envelope.version(),
                    envelope.payload()
                            .getClass()
                            .getSimpleName()
            );

            log.info(
                    "Passing event to WalletEventHandler..."
            );

            walletEventHandler.handle(
                    envelope
            );

            log.info(
                    "Wallet event processing completed successfully. " +
                            "type={}, eventId={}",
                    type,
                    event.eventId()
            );

        } catch (Exception ex) {

            log.error(
                    "❌ FAILED PROCESSING WALLET EVENT. Raw message={}",
                    message,
                    ex
            );

            throw ex;
        }
    }
}