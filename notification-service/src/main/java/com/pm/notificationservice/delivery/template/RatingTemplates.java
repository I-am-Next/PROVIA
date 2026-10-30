package com.pm.notificationservice.delivery.template;

import org.springframework.stereotype.Component;

@Component
public class RatingTemplates {

    public String providerRated() {

        return """
                Thank you for rating your provider.
                Your feedback helps maintain the quality of services on Provia.
                """;

    }

    public String customerRated() {

        return """
                A provider has rated you after completing your booking.
                Your reputation helps build trust within the Provia community.
                """;

    }

}