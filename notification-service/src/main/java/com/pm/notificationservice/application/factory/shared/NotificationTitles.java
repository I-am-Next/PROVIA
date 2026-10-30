package com.pm.notificationservice.application.factory.shared;

public final class NotificationTitles {

    /*
     * Booking
     */
    public static final String BOOKING_CREATED = "Booking Created";
    public static final String BOOKING_ACCEPTED = "Booking Accepted";
    public static final String BOOKING_REJECTED = "Booking Rejected";
    public static final String BOOKING_CANCELLED = "Booking Cancelled";

    /*
     * Quote
     */
    public static final String QUOTE_RECEIVED = "Quotation Received";
    public static final String QUOTE_ACCEPTED = "Quotation Accepted";
    public static final String QUOTE_REJECTED = "Quotation Rejected";

    /*
     * Payment
     */
    public static final String PAYMENT_PENDING = "Payment Pending";
    public static final String PAYMENT_COMPLETED = "Payment Completed";
    public static final String PAYMENT_FAILED = "Payment Failed";

    /*
     * Escrow
     */
    public static final String ESCROW_CREATED = "Escrow Created";
    public static final String MATERIALS_RELEASED = "Materials Released";
    public static final String LABOUR_RELEASED = "Final Payment Released";
    public static final String ESCROW_REFUNDED = "Escrow Refunded";

    /*
     * Job Progress
     */
    public static final String WORK_STARTED = "Work Started";
    public static final String WORK_COMPLETED = "Work Completed";
    public static final String CUSTOMER_CONFIRMED_COMPLETION = "Completion Confirmed";

    /*
     * Wallet
     */
    public static final String WALLET_CREDITED = "Wallet Credited";
    public static final String WITHDRAWAL_REQUESTED = "Withdrawal Requested";
    public static final String WITHDRAWAL_COMPLETED = "Withdrawal Completed";
    public static final String WITHDRAWAL_FAILED = "Withdrawal Failed";

    /*
     * Rating
     */
    public static final String PROVIDER_RATED = "Provider Rated";
    public static final String CUSTOMER_RATED = "Customer Rated";

    /*
     * Chat
     */
    public static final String CHAT_MESSAGE = "New Message";

    /*
     * System
     */
    public static final String SYSTEM = "System Notification";
    public static final String ADMIN = "Administrative Notification";

    private NotificationTitles() {
    }

}