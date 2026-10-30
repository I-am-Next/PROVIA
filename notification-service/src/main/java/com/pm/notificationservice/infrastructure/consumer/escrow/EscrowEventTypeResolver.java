package com.pm.notificationservice.infrastructure.consumer.escrow;

import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.escrow.EscrowCreatedEvent;
import com.pm.contracts.escrow.EscrowRefundedEvent;
import com.pm.contracts.escrow.LabourReleasedEvent;
import com.pm.contracts.escrow.MaterialsReleasedEvent;
import org.springframework.stereotype.Component;

@Component
public class EscrowEventTypeResolver {

    public boolean supports(String eventType) {
        return switch (eventType) {
            case "ESCROW_CREATED",
                 "ESCROW_REFUNDED",
                 "MATERIALS_RELEASED",
                 "LABOUR_RELEASED" -> true;

            default -> false;
        };
    }

    public Class<? extends DomainEvent> resolve(String eventType) {
        return switch (eventType) {
            case "ESCROW_CREATED" ->
                    EscrowCreatedEvent.class;

            case "ESCROW_REFUNDED" ->
                    EscrowRefundedEvent.class;

            case "MATERIALS_RELEASED" ->
                    MaterialsReleasedEvent.class;

            case "LABOUR_RELEASED" ->
                    LabourReleasedEvent.class;

            default -> throw new IllegalArgumentException(
                    "Unsupported escrow event type: " + eventType
            );
        };
    }
}
