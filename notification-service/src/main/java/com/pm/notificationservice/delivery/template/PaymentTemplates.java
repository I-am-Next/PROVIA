package com.pm.notificationservice.delivery.template;

import org.springframework.stereotype.Component;

@Component
public class PaymentTemplates {

    public String paymentPending() {

        return """
                Your payment has been initiated and is awaiting confirmation.
                We will notify you once it has been completed.
                """;

    }

    public String paymentCompleted() {

        return """
                Your payment was completed successfully.
                Thank you for choosing Provia.
                """;

    }

    public String paymentFailed() {

        return """
                Unfortunately, your payment could not be completed.
                Please try again or use another payment method.
                """;

    }

    public String escrowCreated() {

        return """
                Your payment has been secured in escrow.
                Funds will be released according to the platform's payment policy.
                """;

    }

    public String materialsReleased() {

        return """
                The materials portion of your payment has been released successfully.
                """;

    }

    public String labourReleased() {

        return """
                The labour payment has been released successfully.
                Thank you for using Provia.
                """;

    }

    public String escrowRefunded() {

        return """
                Your escrow payment has been refunded successfully.
                The funds should reflect in your account shortly.
                """;

    }

    public String walletCredited() {

        return """
                Your wallet has been credited successfully.
                You can use the funds for future bookings or withdrawals.
                """;

    }

    public String withdrawalRequested() {

        return """
                Your withdrawal request has been received and is being processed.
                """;

    }

    public String withdrawalCompleted() {

        return """
                Your withdrawal has been completed successfully.
                Please check your bank account for the transferred funds.
                """;

    }

    public String withdrawalFailed() {

        return """
                Your withdrawal request could not be completed.
                Please try again or contact support.
                """;

    }

}