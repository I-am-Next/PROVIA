package com.pm.notificationservice.infrastructure.consumer.booking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.common.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventsConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    BookingEventsConsumer.class
            );

    private final ObjectMapper objectMapper;

    private final BookingEventHandler bookingEventHandler;

    private final EventTypeResolver eventTypeResolver;

    public BookingEventsConsumer(
            ObjectMapper objectMapper,
            BookingEventHandler bookingEventHandler,
            EventTypeResolver eventTypeResolver
    ) {
        this.objectMapper = objectMapper;
        this.bookingEventHandler = bookingEventHandler;
        this.eventTypeResolver = eventTypeResolver;
    }

    @KafkaListener(
            topics = "${notification.kafka.bookings}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            String message
    ) throws Exception {

        log.info(
                "========== BOOKING EVENT RECEIVED =========="
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
                    "Detected booking event type={}, version={}",
                    type,
                    version
            );

            /*
             * Not every booking domain event requires
             * a notification.
             *
             * Example:
             * BOOKING_STATUS_CHANGED
             *
             * The UI already derives the appropriate booking
             * state, so Notification Service does not need
             * to create a notification for this event.
             */
            if (!eventTypeResolver.supports(type)) {

                log.info(
                        "Ignoring booking event type={} because " +
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
                    "Booking event deserialized successfully. " +
                            "type={}, version={}, payloadClass={}",
                    envelope.type(),
                    envelope.version(),
                    envelope.payload()
                            .getClass()
                            .getSimpleName()
            );

            log.info(
                    "Passing event to BookingEventHandler..."
            );

            bookingEventHandler.handle(
                    envelope
            );

            log.info(
                    "Booking event processing completed successfully. " +
                            "type={}, eventId={}",
                    type,
                    event.eventId()
            );

        } catch (Exception ex) {

            log.error(
                    "❌ FAILED PROCESSING BOOKING EVENT. Raw message={}",
                    message,
                    ex
            );

            throw ex;
        }
    }
}