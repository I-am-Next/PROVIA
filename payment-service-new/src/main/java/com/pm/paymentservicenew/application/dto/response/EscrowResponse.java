package com.pm.paymentservicenew.application.dto.response;

import com.pm.paymentservicenew.domain.model.escrow.EscrowStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record EscrowResponse(

        UUID escrowId,

        UUID bookingId,

        BigDecimal totalAmount,

        BigDecimal releasedAmount,

        BigDecimal remainingAmount,

        EscrowStatus status

) {
}