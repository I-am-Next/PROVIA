package com.pm.bookingservice.integration.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record EscrowResponse(

        UUID escrowId,

        UUID bookingId,

        BigDecimal totalAmount,

        BigDecimal releasedAmount,

        BigDecimal remainingAmount,

        String status

) {
}