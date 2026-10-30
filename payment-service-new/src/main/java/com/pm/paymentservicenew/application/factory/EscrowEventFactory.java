
package com.pm.paymentservicenew.application.factory;

import com.pm.contracts.escrow.EscrowCreatedEvent;
import com.pm.contracts.escrow.LabourReleasedEvent;
import com.pm.contracts.escrow.MaterialsReleasedEvent;
import com.pm.paymentservicenew.domain.model.escrow.Escrow;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class EscrowEventFactory {

    public EscrowCreatedEvent escrowCreated(Escrow escrow) {
        return new EscrowCreatedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                escrow.getId(),
                escrow.getPaymentId(),
                escrow.getBookingId(),
                escrow.getCustomerId(),
                escrow.getProviderId(),
                escrow.getTotalAmount(),
                escrow.getReleasedAmount(),
                escrow.getStatus().name(),
                escrow.getCreatedAt()
        );
    }

    public MaterialsReleasedEvent materialsReleased(
            Escrow escrow,
            UUID quoteId,
            BigDecimal amount
    ) {
        if (quoteId == null) {
            throw new IllegalArgumentException(
                    "Quote ID is required for MATERIALS_RELEASED."
            );
        }

        return new MaterialsReleasedEvent(
                UUID.randomUUID(),
                2,
                Instant.now(),
                escrow.getId(),
                escrow.getPaymentId(),
                escrow.getBookingId(),
                quoteId,
                escrow.getCustomerId(),
                escrow.getProviderId(),
                amount
        );
    }

    public LabourReleasedEvent labourReleased(
            Escrow escrow,
            BigDecimal amount
    ) {
        return new LabourReleasedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                escrow.getId(),
                escrow.getPaymentId(),
                escrow.getBookingId(),
                escrow.getCustomerId(),
                escrow.getProviderId(),
                amount
        );
    }
}
