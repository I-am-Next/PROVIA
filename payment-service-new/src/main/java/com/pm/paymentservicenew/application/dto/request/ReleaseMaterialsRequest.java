package com.pm.paymentservicenew.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ReleaseMaterialsRequest(

        @NotNull
        UUID quoteId,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal amount

) {
}
