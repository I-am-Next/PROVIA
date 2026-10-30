package com.pm.bookingservice.domain.model;

public enum BookingStatus {

    REQUESTED,
    REJECTED,

    AWAITING_QUOTE,
    QUOTE_SENT,
    QUOTE_ACCEPTED,
    QUOTE_REJECTED,

    PAYMENT_COMPLETED,

    MATERIALS_RELEASED,

    IN_PROGRESS,

    COMPLETED,

    COMPLETION_CONFIRMED,

    CANCELLED;

    public boolean isPaymentCompleted() {
        return this == PAYMENT_COMPLETED;
    }

    public boolean isQuoteAccepted() {
        return this == QUOTE_ACCEPTED;
    }

    public boolean isMaterialsReleased() {
        return this == MATERIALS_RELEASED;
    }

    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }

    public boolean isCompletionConfirmed() {
        return this == COMPLETION_CONFIRMED;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    public boolean isAwaitingQuote() {
        return this == AWAITING_QUOTE;
    }

    public boolean isQuoteSent() {
        return this == QUOTE_SENT;
    }

    public boolean isRequested() {
        return this == REQUESTED;
    }

}