package com.pm.paymentservicenew.integration.dto;

public record BookingAddressResponse(

        String addressLine1,

        String addressLine2,

        String city,

        String state,

        String postalCode,

        String country

) {
}