package com.pm.notificationservice.application.factory.shared;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NotificationActionUrlFactory {

    public String booking(

            UUID bookingId

    ) {

        return "/bookings/" + bookingId;

    }

    public String payment(

            UUID paymentId

    ) {

        return "/payments/" + paymentId;

    }

    public String escrow(

            UUID escrowId

    ) {

        return "/escrows/" + escrowId;

    }

    public String wallet(

            UUID walletId

    ) {

        return "/wallets/" + walletId;

    }

    public String withdrawal(

            UUID withdrawalId

    ) {

        return "/wallets/withdrawals/" + withdrawalId;

    }

    public String rating(

            UUID ratingId

    ) {

        return "/ratings/" + ratingId;

    }

    public String chat(

            UUID conversationId

    ) {

        return "/chat/" + conversationId;

    }

    public String notifications() {

        return "/notifications";

    }

    public String dashboard() {

        return "/dashboard";

    }

    public String adminDashboard() {

        return "/admin";

    }

}