package com.pm.notificationservice.infrastructure.consumer.quote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.booking.QuoteAcceptedEvent;
import com.pm.contracts.booking.QuoteReceivedEvent;
import com.pm.contracts.booking.QuoteRejectedEvent;
import com.pm.contracts.common.EventEnvelope;
import com.pm.notificationservice.application.service.EventProcessingService;
import com.pm.notificationservice.application.service.NotificationCreationService;
import com.pm.notificationservice.infrastructure.consumer.AbstractEventHandler;
import org.springframework.stereotype.Component;

@Component
public class QuoteEventHandler extends AbstractEventHandler {


    public QuoteEventHandler(
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


        switch (envelope.type()) {


            case "QUOTE_RECEIVED" -> {

                QuoteReceivedEvent event =
                        convert(
                                envelope.payload(),
                                QuoteReceivedEvent.class
                        );


                process(
                        event,
                        () -> notificationCreationService.createQuoteReceivedNotification(event)
                );

            }


            case "QUOTE_ACCEPTED" -> {

                QuoteAcceptedEvent event =
                        convert(
                                envelope.payload(),
                                QuoteAcceptedEvent.class
                        );


                process(
                        event,
                        () -> notificationCreationService.createQuoteAcceptedNotification(event)
                );

            }


            case "QUOTE_REJECTED" -> {

                QuoteRejectedEvent event =
                        convert(
                                envelope.payload(),
                                QuoteRejectedEvent.class
                        );


                process(
                        event,
                        () -> notificationCreationService.createQuoteRejectedNotification(event)
                );

            }


            default -> throw new IllegalArgumentException(
                    "Unsupported quote event type: " + envelope.type()
            );

        }

    }

}