package com.pm.bookingservice.application.dto.request.quote;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateQuoteRequest(

        @NotNull
        @DecimalMin("0.0")
        BigDecimal labourAmount,


        @NotNull
        @DecimalMin("0.0")
        BigDecimal materialsAmount,


        @NotBlank
        String description,


        @NotNull
        @Positive
        Integer estimatedDurationDays

) {
}