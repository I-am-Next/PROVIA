package com.pm.notificationservice.infrastructure.consumer.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.admin.AdminNotificationEvent;
import com.pm.contracts.common.EventEnvelope;
import com.pm.notificationservice.application.service.EventProcessingService;
import com.pm.notificationservice.application.service.NotificationCreationService;
import com.pm.notificationservice.infrastructure.consumer.AbstractEventHandler;
import org.springframework.stereotype.Component;

@Component
public class AdminEventHandler extends AbstractEventHandler {


    public AdminEventHandler(
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


            case "ADMIN_NOTIFICATION" -> {

                AdminNotificationEvent event =
                        convert(
                                envelope.payload(),
                                AdminNotificationEvent.class
                        );


                process(
                        event,
                        () -> notificationCreationService.createAdminNotification(event)
                );

            }


            default -> throw new IllegalArgumentException(
                    "Unsupported admin event type: " + envelope.type()
            );

        }

    }

}