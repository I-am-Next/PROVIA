package com.pm.notificationservice.infrastructure.consumer.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.booking.*;
import com.pm.contracts.common.EventEnvelope;
import com.pm.notificationservice.application.service.EventProcessingService;
import com.pm.notificationservice.application.service.NotificationCreationService;
import com.pm.notificationservice.infrastructure.consumer.AbstractEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BookingEventHandler extends AbstractEventHandler {

    private static final Logger log =
            LoggerFactory.getLogger(BookingEventHandler.class);

    public BookingEventHandler(
            ObjectMapper objectMapper,
            NotificationCreationService notificationCreationService,
            EventProcessingService eventProcessingService
    ) {
        super(
                objectMapper,
                notificationCreationService,
                eventProcessingService
        );
    }

    public void handle(
            EventEnvelope<?> envelope
    ) {

        log.info(
                ">>> BookingEventHandler.handle() ENTERED. type={}, version={}, payloadClass={}",
                envelope.type(),
                envelope.version(),
                envelope.payload() != null
                        ? envelope.payload().getClass().getSimpleName()
                        : "null"
        );

        switch (envelope.type()) {

            case "BOOKING_CREATED" -> {

                log.info(
                        ">>> BOOKING_CREATED case entered"
                );

                BookingCreatedEvent event =
                        convert(
                                envelope.payload(),
                                BookingCreatedEvent.class
                        );

                log.info(
                        ">>> BOOKING_CREATED payload converted successfully. bookingId={}, customerId={}, providerId={}",
                        event.bookingId(),
                        event.customerId(),
                        event.providerId()
                );

                log.info(
                        ">>> Calling process() for BOOKING_CREATED. eventId={}",
                        event.eventId()
                );

                process(
                        event,
                        () -> {
                            log.info(
                                    ">>> Calling createBookingCreatedNotification(). bookingId={}",
                                    event.bookingId()
                            );

                            notificationCreationService
                                    .createBookingCreatedNotification(event);

                            log.info(
                                    ">>> createBookingCreatedNotification() completed. bookingId={}",
                                    event.bookingId()
                            );
                        }
                );

                log.info(
                        ">>> BOOKING_CREATED case completed. eventId={}",
                        event.eventId()
                );
            }

            case "BOOKING_ACCEPTED" -> {

                log.info(
                        ">>> BOOKING_ACCEPTED case entered"
                );

                BookingAcceptedEvent event =
                        convert(
                                envelope.payload(),
                                BookingAcceptedEvent.class
                        );

                log.info(
                        ">>> BOOKING_ACCEPTED payload converted successfully. bookingId={}",
                        event.bookingId()
                );

                log.info(
                        ">>> Calling process() for BOOKING_ACCEPTED. eventId={}",
                        event.eventId()
                );

                process(
                        event,
                        () -> {
                            log.info(
                                    ">>> Calling createBookingAcceptedNotification(). bookingId={}",
                                    event.bookingId()
                            );

                            notificationCreationService
                                    .createBookingAcceptedNotification(event);

                            log.info(
                                    ">>> createBookingAcceptedNotification() completed. bookingId={}",
                                    event.bookingId()
                            );
                        }
                );

                log.info(
                        ">>> BOOKING_ACCEPTED case completed. eventId={}",
                        event.eventId()
                );
            }

            case "BOOKING_REJECTED" -> {

                log.info(
                        ">>> BOOKING_REJECTED case entered"
                );

                BookingRejectedEvent event =
                        convert(
                                envelope.payload(),
                                BookingRejectedEvent.class
                        );

                log.info(
                        ">>> BOOKING_REJECTED payload converted successfully. bookingId={}",
                        event.bookingId()
                );

                log.info(
                        ">>> Calling process() for BOOKING_REJECTED. eventId={}",
                        event.eventId()
                );

                process(
                        event,
                        () -> {
                            log.info(
                                    ">>> Calling createBookingRejectedNotification(). bookingId={}",
                                    event.bookingId()
                            );

                            notificationCreationService
                                    .createBookingRejectedNotification(event);

                            log.info(
                                    ">>> createBookingRejectedNotification() completed. bookingId={}",
                                    event.bookingId()
                            );
                        }
                );

                log.info(
                        ">>> BOOKING_REJECTED case completed. eventId={}",
                        event.eventId()
                );
            }

            case "BOOKING_CANCELLED" -> {

                log.info(
                        ">>> BOOKING_CANCELLED case entered"
                );

                BookingCancelledEvent event =
                        convert(
                                envelope.payload(),
                                BookingCancelledEvent.class
                        );

                log.info(
                        ">>> BOOKING_CANCELLED payload converted successfully. bookingId={}",
                        event.bookingId()
                );

                log.info(
                        ">>> Calling process() for BOOKING_CANCELLED. eventId={}",
                        event.eventId()
                );

                process(
                        event,
                        () -> {
                            log.info(
                                    ">>> Calling createBookingCancelledNotification(). bookingId={}",
                                    event.bookingId()
                            );

                            notificationCreationService
                                    .createBookingCancelledNotification(event);

                            log.info(
                                    ">>> createBookingCancelledNotification() completed. bookingId={}",
                                    event.bookingId()
                            );
                        }
                );

                log.info(
                        ">>> BOOKING_CANCELLED case completed. eventId={}",
                        event.eventId()
                );
            }


            case "BOOKING_STARTED" -> {
                BookingStartedEvent event = convert(
                        envelope.payload(),
                        BookingStartedEvent.class
                );

                process(
                        event,
                        () -> notificationCreationService
                                .createWorkStartedNotification(event)
                );
            }

            case "BOOKING_COMPLETED" -> {
                BookingCompletedEvent event = convert(
                        envelope.payload(),
                        BookingCompletedEvent.class
                );

                process(
                        event,
                        () -> notificationCreationService
                                .createWorkCompletedNotification(event)
                );
            }


            case "CUSTOMER_CONFIRMED_COMPLETION" -> {

                log.info(
                        ">>> CUSTOMER_CONFIRMED_COMPLETION case entered"
                );

                CustomerConfirmedCompletionEvent event =
                        convert(
                                envelope.payload(),
                                CustomerConfirmedCompletionEvent.class
                        );

                log.info(
                        ">>> CUSTOMER_CONFIRMED_COMPLETION payload converted successfully. bookingId={}",
                        event.bookingId()
                );

                log.info(
                        ">>> Calling process() for CUSTOMER_CONFIRMED_COMPLETION. eventId={}",
                        event.eventId()
                );

                process(
                        event,
                        () -> {
                            log.info(
                                    ">>> Calling createCustomerConfirmedCompletionNotification(). bookingId={}",
                                    event.bookingId()
                            );

                            notificationCreationService
                                    .createCustomerConfirmedCompletionNotification(event);

                            log.info(
                                    ">>> createCustomerConfirmedCompletionNotification() completed. bookingId={}",
                                    event.bookingId()
                            );
                        }
                );

                log.info(
                        ">>> CUSTOMER_CONFIRMED_COMPLETION case completed. eventId={}",
                        event.eventId()
                );
            }

            default -> {

                log.warn(
                        ">>> UNKNOWN BOOKING EVENT TYPE: {}",
                        envelope.type()
                );
            }
        }

        log.info(
                ">>> BookingEventHandler.handle() EXITED. type={}",
                envelope.type()
        );
    }
}