package com.pm.providerservice.api.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProviderServiceRequest(

        @NotNull
        UUID serviceItemId,

        @NotNull
        @DecimalMin(
                value = "0.0",
                inclusive = false
        )
        BigDecimal price

) {
}