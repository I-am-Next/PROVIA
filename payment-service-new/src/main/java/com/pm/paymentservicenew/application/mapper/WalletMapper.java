package com.pm.paymentservicenew.application.mapper;

import com.pm.paymentservicenew.application.dto.response.WalletResponse;
import com.pm.paymentservicenew.domain.model.wallet.Wallet;
import org.springframework.stereotype.Component;

@Component
public record WalletMapper() {

    public WalletResponse toResponse(
            Wallet wallet
    ) {

        return new WalletResponse(

                wallet.getId(),

                wallet.getProviderId(),

                wallet.getAvailableBalance(),

                wallet.getPendingBalance(),

                wallet.getReservedBalance(),

                wallet.getTotalBalance()

        );

    }

}