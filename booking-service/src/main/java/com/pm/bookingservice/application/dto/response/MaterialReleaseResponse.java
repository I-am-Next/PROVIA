package com.pm.bookingservice.application.dto.response;

import com.pm.bookingservice.integration.dto.EscrowResponse;

import java.util.UUID;

public record MaterialReleaseResponse(

        UUID bookingId,

        UUID quoteId,

        boolean materialsReleased,

        EscrowResponse escrow

) {
}