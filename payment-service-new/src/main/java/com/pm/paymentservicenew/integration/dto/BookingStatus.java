package com.pm.paymentservicenew.integration.dto;

public enum BookingStatus {

    REQUESTED,
    REJECTED,
    CANCELLED,

    AWAITING_QUOTE,
    QUOTE_SENT,
    QUOTE_ACCEPTED,
    QUOTE_REJECTED,

    ACCEPTED,
    IN_PROGRESS,
    COMPLETED

}