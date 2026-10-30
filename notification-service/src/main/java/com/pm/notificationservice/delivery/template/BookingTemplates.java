package com.pm.notificationservice.delivery.template;

import org.springframework.stereotype.Component;

@Component
public class BookingTemplates {

    public String bookingCreated() {

        return """
                Your booking has been created successfully.
                A provider will review your request shortly.
                """;

    }

    public String bookingAccepted() {

        return """
                Good news! Your booking has been accepted by the provider.
                You can now continue with the next steps.
                """;

    }

    public String bookingRejected() {

        return """
                Unfortunately, your booking request was declined.
                You may choose another provider and try again.
                """;

    }

    public String bookingCancelled() {

        return """
                Your booking has been cancelled successfully.
                If this was unintentional, you can create a new booking anytime.
                """;

    }

    public String quoteReceived() {

        return """
                Your provider has sent you a quotation.
                Review and accept or reject it from your dashboard.
                """;

    }

    public String quoteAccepted() {

        return """
                The quotation has been accepted successfully.
                Your provider has been notified.
                """;

    }

    public String quoteRejected() {

        return """
                The quotation has been rejected.
                The provider has been notified.
                """;

    }

    public String workStarted() {

        return """
                Your provider has started working on your booking.
                You will receive updates as work progresses.
                """;

    }

    public String workCompleted() {

        return """
                Your provider has marked the work as completed.
                Please review the work and confirm completion.
                """;

    }

    public String customerConfirmedCompletion() {

        return """
                Completion has been confirmed successfully.
                Payment settlement will now begin.
                """;

    }

}