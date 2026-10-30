package com.pm.bookingservice.application.dto.response;

public record BookingAddressResponse(
        String addressLine1,

        String addressLine2,

        String city,

        String state,

        String postalCode,

        String country
) { }
