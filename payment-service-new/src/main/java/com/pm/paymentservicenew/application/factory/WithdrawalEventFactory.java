package com.pm.paymentservicenew.application.factory;

import com.pm.contracts.wallet.WithdrawalCompletedEvent;
import com.pm.contracts.wallet.WithdrawalFailedEvent;
import com.pm.contracts.wallet.WithdrawalRequestedEvent;
import com.pm.paymentservicenew.domain.model.withdrawal.Withdrawal;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class WithdrawalEventFactory {

    public WithdrawalRequestedEvent withdrawalRequested(

            Withdrawal withdrawal

    ) {

        return new WithdrawalRequestedEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                withdrawal.getId(),

                withdrawal.getWalletId(),

                withdrawal.getProviderId(),

                withdrawal.getAmount()

        );

    }

    public WithdrawalCompletedEvent withdrawalCompleted(

            Withdrawal withdrawal

    ) {

        return new WithdrawalCompletedEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                withdrawal.getId(),

                withdrawal.getWalletId(),

                withdrawal.getProviderId(),

                withdrawal.getAmount()

        );

    }

    public WithdrawalFailedEvent withdrawalFailed(

            Withdrawal withdrawal

    ) {

        return new WithdrawalFailedEvent(

                UUID.randomUUID(),

                1,

                Instant.now(),

                withdrawal.getId(),

                withdrawal.getWalletId(),

                withdrawal.getProviderId(),

                withdrawal.getAmount(),

                withdrawal.getFailureReason()

        );

    }

}