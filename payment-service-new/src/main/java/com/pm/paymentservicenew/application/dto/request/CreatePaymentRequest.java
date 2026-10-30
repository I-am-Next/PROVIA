package com.pm.paymentservicenew.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePaymentRequest(

        @NotNull
        UUID bookingId,

        @NotBlank
        String customerEmail

) {
}