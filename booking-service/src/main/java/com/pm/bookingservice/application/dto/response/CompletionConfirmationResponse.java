package com.pm.bookingservice.application.dto.response;

import com.pm.bookingservice.integration.dto.EscrowResponse;

public record CompletionConfirmationResponse(
        BookingResponse booking,

        EscrowResponse escrow
) {
}
