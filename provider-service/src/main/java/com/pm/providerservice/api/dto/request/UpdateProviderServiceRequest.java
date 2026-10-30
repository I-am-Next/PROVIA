package com.pm.providerservice.api.dto.request;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdateProviderServiceRequest(

        @DecimalMin(
                value = "0.0",
                inclusive = false
        )
        BigDecimal price

) {
}