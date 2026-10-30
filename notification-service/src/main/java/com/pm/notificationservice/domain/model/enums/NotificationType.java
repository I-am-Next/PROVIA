package com.pm.notificationservice.domain.model.enums;

public enum NotificationType {

    /*
     * Booking
     */
    BOOKING_CREATED,
    BOOKING_ACCEPTED,
    BOOKING_REJECTED,
    BOOKING_CANCELLED,

    /*
     * Quote
     */
    QUOTE_RECEIVED,
    QUOTE_ACCEPTED,
    QUOTE_REJECTED,

    /*
     * Payment
     */
    PAYMENT_PENDING,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,

    /*
     * Escrow
     */
    ESCROW_CREATED,
    MATERIALS_RELEASED,
    LABOUR_RELEASED,
    ESCROW_REFUNDED,

    /*
     * Job Progress
     */
    WORK_STARTED,
    WORK_COMPLETED,
    CUSTOMER_CONFIRMED_COMPLETION,

    /*
     * Wallet
     */
    WALLET_CREDITED,
    WITHDRAWAL_REQUESTED,
    WITHDRAWAL_COMPLETED,
    WITHDRAWAL_FAILED,

    /*
     * Rating
     */
    PROVIDER_RATED,
    CUSTOMER_RATED,

    /*
     * Chat
     */
    CHAT_MESSAGE,

    /*
     * Administrative / System
     */
    SYSTEM,
    ADMIN

}