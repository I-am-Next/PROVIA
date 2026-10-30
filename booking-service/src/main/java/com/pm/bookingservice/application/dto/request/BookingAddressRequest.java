package com.pm.bookingservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BookingAddressRequest(
        @NotBlank(message = "Address line 1 is required")
        String addressLine1,

        String addressLine2,

        @NotBlank(message = "City is required")
        String city,

        String state,

        String postalCode,

        @NotBlank(message = "Country is required")
        String country
) { }
