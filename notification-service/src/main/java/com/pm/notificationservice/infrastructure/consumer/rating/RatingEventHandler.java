package com.pm.notificationservice.infrastructure.consumer.rating;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.common.EventEnvelope;
import com.pm.contracts.rating.CustomerRatedEvent;
import com.pm.contracts.rating.ProviderRatedEvent;
import com.pm.notificationservice.application.service.EventProcessingService;
import com.pm.notificationservice.application.service.NotificationCreationService;
import com.pm.notificationservice.infrastructure.consumer.AbstractEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RatingEventHandler
        extends AbstractEventHandler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    RatingEventHandler.class
            );

    public RatingEventHandler(
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
                ">>> RatingEventHandler.handle() ENTERED. " +
                        "type={}, version={}, payloadClass={}",
                envelope.type(),
                envelope.version(),
                envelope.payload() != null
                        ? envelope.payload()
                        .getClass()
                        .getSimpleName()
                        : "null"
        );

        switch (envelope.type()) {

            case "CUSTOMER_RATED" -> {

                log.info(
                        ">>> CUSTOMER_RATED case entered"
                );

                CustomerRatedEvent event =
                        convert(
                                envelope.payload(),
                                CustomerRatedEvent.class
                        );

                log.info(
                        ">>> CUSTOMER_RATED payload converted successfully. " +
                                "ratingId={}, bookingId={}, providerId={}, customerId={}",
                        event.ratingId(),
                        event.bookingId(),
                        event.providerId(),
                        event.customerId()
                );

                process(
                        event,
                        () -> {

                            log.info(
                                    ">>> Calling createCustomerRatedNotification(). " +
                                            "ratingId={}",
                                    event.ratingId()
                            );

                            notificationCreationService
                                    .createCustomerRatedNotification(
                                            event
                                    );

                            log.info(
                                    ">>> createCustomerRatedNotification() completed. " +
                                            "ratingId={}",
                                    event.ratingId()
                            );
                        }
                );

                log.info(
                        ">>> CUSTOMER_RATED case completed. eventId={}",
                        event.eventId()
                );
            }

            case "PROVIDER_RATED" -> {

                log.info(
                        ">>> PROVIDER_RATED case entered"
                );

                ProviderRatedEvent event =
                        convert(
                                envelope.payload(),
                                ProviderRatedEvent.class
                        );

                log.info(
                        ">>> PROVIDER_RATED payload converted successfully. " +
                                "ratingId={}, bookingId={}, providerId={}, customerId={}",
                        event.ratingId(),
                        event.bookingId(),
                        event.providerId(),
                        event.customerId()
                );

                process(
                        event,
                        () -> {

                            log.info(
                                    ">>> Calling createProviderRatedNotification(). " +
                                            "ratingId={}",
                                    event.ratingId()
                            );

                            notificationCreationService
                                    .createProviderRatedNotification(
                                            event
                                    );

                            log.info(
                                    ">>> createProviderRatedNotification() completed. " +
                                            "ratingId={}",
                                    event.ratingId()
                            );
                        }
                );

                log.info(
                        ">>> PROVIDER_RATED case completed. eventId={}",
                        event.eventId()
                );
            }

            default -> {

                log.warn(
                        ">>> UNKNOWN RATING EVENT TYPE: {}",
                        envelope.type()
                );
            }
        }

        log.info(
                ">>> RatingEventHandler.handle() EXITED. type={}",
                envelope.type()
        );
    }
}