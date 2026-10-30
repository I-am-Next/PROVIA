package com.pm.notificationservice.infrastructure.consumer.wallet;

import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.wallet.WalletCreditedEvent;
import com.pm.contracts.wallet.WalletWithdrawalCancelledEvent;
import com.pm.contracts.wallet.WalletWithdrawalCompletedEvent;
import com.pm.contracts.wallet.WithdrawalCompletedEvent;
import com.pm.contracts.wallet.WithdrawalFailedEvent;
import com.pm.contracts.wallet.WithdrawalRequestedEvent;
import org.springframework.stereotype.Component;

@Component
public class WalletEventTypeResolver {

    public boolean supports(String eventType) {
        return switch (eventType) {
            case "WALLET_CREDITED",
                 "WITHDRAWAL_REQUESTED",
                 "WITHDRAWAL_COMPLETED",
                 "WITHDRAWAL_FAILED",
                 "WALLET_WITHDRAWAL_COMPLETED",
                 "WALLET_WITHDRAWAL_CANCELLED" -> true;

            default -> false;
        };
    }

    public Class<? extends DomainEvent> resolve(String eventType) {
        return switch (eventType) {
            case "WALLET_CREDITED" ->
                    WalletCreditedEvent.class;

            case "WITHDRAWAL_REQUESTED" ->
                    WithdrawalRequestedEvent.class;

            case "WITHDRAWAL_COMPLETED" ->
                    WithdrawalCompletedEvent.class;

            case "WITHDRAWAL_FAILED" ->
                    WithdrawalFailedEvent.class;

            case "WALLET_WITHDRAWAL_COMPLETED" ->
                    WalletWithdrawalCompletedEvent.class;

            case "WALLET_WITHDRAWAL_CANCELLED" ->
                    WalletWithdrawalCancelledEvent.class;

            default -> throw new IllegalArgumentException(
                    "Unsupported wallet event type: " + eventType
            );
        };
    }
}
