package com.pm.notificationservice.application.factory.shared;

import java.math.BigDecimal;

public final class NotificationMessages {

    private NotificationMessages() {
    }

    /*
     * Booking
     */

    public static String bookingCreated() {

        return "Your booking has been created successfully.";

    }
    public static String bookingAccepted() {

        return "Your booking has been accepted by the provider.";

    }

    public static String bookingRejected() {

        return "Unfortunately, your booking was rejected.";

    }

    public static String bookingCancelled() {
        return "Your booking has been cancelled.";

    }


    /*
     * Quote
     */

    public static String quoteReceived() {

        return "A provider has sent you a quotation for your booking.";

    }

    public static String quoteAccepted() {

        return "Your quotation has been accepted.";

    }

    public static String quoteRejected() {

        return "Your quotation has been rejected.";

    }

    /*
     * Payment
     */

    public static String paymentPending(

            BigDecimal amount,

            String currency

    ) {

        return "Your payment of "
                + amount
                + " "
                + currency
                + " is awaiting confirmation.";

    }

    public static String paymentCompleted(

            BigDecimal amount,

            String currency

    ) {

        return "Your payment of "
                + amount
                + " "
                + currency
                + " has been completed successfully.";

    }

    public static String paymentFailed(

            BigDecimal amount,

            String currency,

            String reason

    ) {

        return "Your payment of "
                + amount
                + " "
                + currency
                + " could not be completed. Reason: "
                + reason;

    }

    /*
     * Escrow
     */

    public static String escrowCreated() {

        return "Funds have been secured in escrow.";

    }

    public static String materialsReleased(

            BigDecimal amount

    ) {

        return "Materials payment of "
                + amount
                + " has been released.";

    }

    public static String labourReleased(

            BigDecimal amount

    ) {

        return "Final labour payment of "
                + amount
                + " has been released.";

    }

    public static String escrowRefunded(

            BigDecimal amount

    ) {

        return "Escrow funds of "
                + amount
                + " have been refunded.";

    }

    /*
     * Job Progress
     */

    public static String workStarted() {

        return "The provider has started working on your booking.";

    }

    public static String workCompleted() {

        return "The provider has completed the work.";

    }

    public static String customerConfirmedCompletion() {

        return "The customer has confirmed the completed work.";

    }

    /*
     * Wallet
     */

    public static String walletCredited(

            BigDecimal amount

    ) {

        return "Your wallet has been credited with "
                + amount
                + ".";

    }

    public static String withdrawalRequested(

            BigDecimal amount

    ) {

        return "Your withdrawal request of "
                + amount
                + " has been received.";

    }

    public static String withdrawalCompleted(

            BigDecimal amount

    ) {

        return "Your withdrawal of "
                + amount
                + " has been completed successfully.";

    }

    public static String withdrawalFailed(

            BigDecimal amount,

            String reason

    ) {

        return "Your withdrawal of "
                + amount
                + " could not be completed. Reason: "
                + reason;

    }

    /*
     * Rating
     */

    public static String providerRated(

            Integer rating

    ) {

        return "You received a new "
                + rating
                + "-star rating.";

    }

    public static String customerRated() {

        return "Thank you for rating your experience.";

    }

    /*
     * Chat
     */

    public static String chatMessage(

            String senderName

    ) {

        return senderName
                + " sent you a new message.";

    }

    /*
     * Administrative / System
     */

    public static String system(

            String message

    ) {

        return message;

    }

    public static String admin(

            String message

    ) {

        return message;

    }

}