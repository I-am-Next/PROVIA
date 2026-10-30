package com.pm.bookingservice.application.dto.response;

public record BookingSummaryResponse(

        long total,

        long awaitingQuote,

        long quoted,

        long inProgress,

        long completed,

        long cancelled

) {
}