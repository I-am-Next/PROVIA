package com.pm.notificationservice.infrastructure.consumer.escrow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.common.EventEnvelope;
import com.pm.contracts.escrow.EscrowCreatedEvent;
import com.pm.contracts.escrow.EscrowRefundedEvent;
import com.pm.contracts.escrow.LabourReleasedEvent;
import com.pm.contracts.escrow.MaterialsReleasedEvent;
import com.pm.notificationservice.application.service.EventProcessingService;
import com.pm.notificationservice.application.service.NotificationCreationService;
import com.pm.notificationservice.infrastructure.consumer.AbstractEventHandler;
import org.springframework.stereotype.Component;

@Component
public class EscrowEventHandler extends AbstractEventHandler {


    public EscrowEventHandler(
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


            case "ESCROW_CREATED" -> {

                EscrowCreatedEvent event =
                        convert(
                                envelope.payload(),
                                EscrowCreatedEvent.class
                        );


                process(
                        event,
                        () -> notificationCreationService.createEscrowCreatedNotification(event)
                );

            }


            case "MATERIALS_RELEASED" -> {

                MaterialsReleasedEvent event =
                        convert(
                                envelope.payload(),
                                MaterialsReleasedEvent.class
                        );


                process(
                        event,
                        () -> notificationCreationService.createMaterialsReleasedNotification(event)
                );

            }


            case "LABOUR_RELEASED" -> {

                LabourReleasedEvent event =
                        convert(
                                envelope.payload(),
                                LabourReleasedEvent.class
                        );


                process(
                        event,
                        () -> notificationCreationService.createLabourReleasedNotification(event)
                );

            }


            case "ESCROW_REFUNDED" -> {

                EscrowRefundedEvent event =
                        convert(
                                envelope.payload(),
                                EscrowRefundedEvent.class
                        );


                process(
                        event,
                        () -> notificationCreationService.createEscrowRefundedNotification(event)
                );

            }


            default -> throw new IllegalArgumentException(
                    "Unsupported escrow event type: " + envelope.type()
            );

        }

    }

}