package com.pm.paymentservicenew.application.factory;

import com.pm.contracts.wallet.WalletCreditedEvent;
import com.pm.contracts.wallet.WalletFundsReservedEvent;
import com.pm.contracts.wallet.WalletWithdrawalCancelledEvent;
import com.pm.contracts.wallet.WalletWithdrawalCompletedEvent;
import com.pm.paymentservicenew.domain.model.wallet.Wallet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class WalletEventFactory {

    public WalletCreditedEvent walletCredited(
            Wallet wallet,
            UUID operationId,
            UUID escrowId,
            BigDecimal amount
    ) {
        return new WalletCreditedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                wallet.getId(),
                wallet.getProviderId(),
                operationId,
                escrowId,
                amount,
                wallet.getAvailableBalance()
        );
    }

    public WalletFundsReservedEvent walletFundsReserved(
            Wallet wallet,
            UUID withdrawalId,
            BigDecimal amount
    ) {
        return new WalletFundsReservedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                wallet.getId(),
                wallet.getProviderId(),
                withdrawalId,
                amount
        );
    }

    public WalletWithdrawalCompletedEvent walletWithdrawalCompleted(
            Wallet wallet,
            UUID withdrawalId,
            BigDecimal amount
    ) {
        return new WalletWithdrawalCompletedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                wallet.getId(),
                wallet.getProviderId(),
                withdrawalId,
                amount
        );
    }

    public WalletWithdrawalCancelledEvent walletWithdrawalCancelled(
            Wallet wallet,
            UUID withdrawalId,
            BigDecimal amount
    ) {
        return new WalletWithdrawalCancelledEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                wallet.getId(),
                wallet.getProviderId(),
                withdrawalId,
                amount
        );
    }
}
