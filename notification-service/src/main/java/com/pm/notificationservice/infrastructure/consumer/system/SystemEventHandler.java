package com.pm.notificationservice.infrastructure.consumer.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.common.EventEnvelope;
import com.pm.contracts.system.SystemNotificationEvent;
import com.pm.notificationservice.application.service.EventProcessingService;
import com.pm.notificationservice.application.service.NotificationCreationService;
import com.pm.notificationservice.infrastructure.consumer.AbstractEventHandler;
import org.springframework.stereotype.Component;

@Component
public class SystemEventHandler extends AbstractEventHandler {


    public SystemEventHandler(
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


            case "SYSTEM_NOTIFICATION" -> {

                SystemNotificationEvent event =
                        convert(
                                envelope.payload(),
                                SystemNotificationEvent.class
                        );


                process(
                        event,
                        () -> notificationCreationService.createSystemNotification(event)
                );

            }


            default -> throw new IllegalArgumentException(
                    "Unsupported system event type: " + envelope.type()
            );

        }

    }

}