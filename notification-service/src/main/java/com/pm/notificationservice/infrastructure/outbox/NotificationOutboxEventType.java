package com.pm.notificationservice.infrastructure.outbox;

public enum NotificationOutboxEventType {

    /*
     * Booking
     */
    BOOKING_CREATED,
    BOOKING_ACCEPTED,
    BOOKING_REJECTED,
    BOOKING_CANCELLED,

    /*
     * Quotes
     */
    QUOTE_RECEIVED,
    QUOTE_ACCEPTED,
    QUOTE_REJECTED,

    /*
     * Work
     */
    WORK_STARTED,
    WORK_COMPLETED,
    CUSTOMER_CONFIRMED_COMPLETION,

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
    CHAT_MESSAGE_RECEIVED,

    /*
     * Notifications
     */
    SYSTEM_NOTIFICATION,
    ADMIN_NOTIFICATION

}