package com.pm.bookingservice.integration.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ReleaseMaterialsRequest(
        UUID quoteId,
        BigDecimal amount
) {
}
