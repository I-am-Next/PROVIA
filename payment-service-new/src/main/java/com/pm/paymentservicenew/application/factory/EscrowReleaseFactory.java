
package com.pm.paymentservicenew.application.factory;

import com.pm.paymentservicenew.domain.model.escrow.Escrow;
import com.pm.paymentservicenew.domain.model.escrow.EscrowRelease;
import com.pm.paymentservicenew.domain.model.escrow.EscrowReleaseType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class EscrowReleaseFactory {

    public EscrowRelease materialsRelease(
            Escrow escrow,
            UUID quoteId,
            BigDecimal amount
    ) {
        if (quoteId == null) {
            throw new IllegalArgumentException(
                    "Quote ID is required for a materials release."
            );
        }

        return EscrowRelease.create(
                escrow.getId(),
                escrow.getBookingId(),
                quoteId,
                escrow.getProviderId(),
                EscrowReleaseType.MATERIALS,
                amount,
                "Materials advance"
        );
    }

    public EscrowRelease labourRelease(
            Escrow escrow,
            BigDecimal amount
    ) {
        return EscrowRelease.create(
                escrow.getId(),
                escrow.getBookingId(),
                null,
                escrow.getProviderId(),
                EscrowReleaseType.LABOUR,
                amount,
                "Labour payment"
        );
    }
}
