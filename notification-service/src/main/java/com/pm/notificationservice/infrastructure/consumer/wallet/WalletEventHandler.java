package com.pm.notificationservice.infrastructure.consumer.wallet;

import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.common.EventEnvelope;
import com.pm.contracts.wallet.WalletCreditedEvent;
import com.pm.contracts.wallet.WalletWithdrawalCompletedEvent;
import com.pm.contracts.wallet.WithdrawalCompletedEvent;
import com.pm.contracts.wallet.WithdrawalFailedEvent;
import com.pm.contracts.wallet.WithdrawalRequestedEvent;
import com.pm.notificationservice.application.service.NotificationCreationService;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class WalletEventHandler {

    private final NotificationCreationService notificationCreationService;

    public WalletEventHandler(
            NotificationCreationService notificationCreationService
    ) {
        this.notificationCreationService = Objects.requireNonNull(
                notificationCreationService,
                "notificationCreationService must not be null"
        );
    }

    public void handle(
            EventEnvelope<? extends DomainEvent> envelope
    ) {

        Objects.requireNonNull(
                envelope,
                "Wallet event envelope must not be null"
        );

        switch (envelope.type()) {

            case "WALLET_CREDITED" -> {
                WalletCreditedEvent event = convert(
                        envelope.payload(),
                        WalletCreditedEvent.class
                );

                notificationCreationService
                        .createWalletCreditedNotification(event);
            }

            case "WITHDRAWAL_REQUESTED" -> {
                WithdrawalRequestedEvent event = convert(
                        envelope.payload(),
                        WithdrawalRequestedEvent.class
                );

                notificationCreationService
                        .createWithdrawalRequestedNotification(event);
            }

            case "WITHDRAWAL_COMPLETED" -> {
                WithdrawalCompletedEvent event = convert(
                        envelope.payload(),
                        WithdrawalCompletedEvent.class
                );

                notificationCreationService
                        .createWithdrawalCompletedNotification(event);
            }

            case "WALLET_WITHDRAWAL_COMPLETED" -> {
                WalletWithdrawalCompletedEvent event = convert(
                        envelope.payload(),
                        WalletWithdrawalCompletedEvent.class
                );

                notificationCreationService
                        .createWithdrawalCompletedNotification(event);
            }

            case "WITHDRAWAL_FAILED" -> {
                WithdrawalFailedEvent event = convert(
                        envelope.payload(),
                        WithdrawalFailedEvent.class
                );

                notificationCreationService
                        .createWithdrawalFailedNotification(event);
            }

            default -> throw new IllegalArgumentException(
                    "Unsupported wallet event type: " + envelope.type()
            );
        }
    }

    private <T extends DomainEvent> T convert(
            DomainEvent payload,
            Class<T> eventClass
    ) {

        if (eventClass.isInstance(payload)) {
            return eventClass.cast(payload);
        }

        throw new IllegalArgumentException(
                "Expected payload of type "
                        + eventClass.getSimpleName()
                        + " but received "
                        + (
                        payload == null
                                ? "null"
                                : payload.getClass().getSimpleName()
                )
        );
    }
}