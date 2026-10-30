package com.pm.paymentservicenew.application.mapper;

import com.pm.paymentservicenew.application.dto.response.WalletTransactionResponse;
import com.pm.paymentservicenew.domain.model.wallet.WalletTransaction;
import org.springframework.stereotype.Component;

@Component
public class WalletTransactionMapper {

    public WalletTransactionResponse toResponse(

            WalletTransaction transaction

    ) {

        return new WalletTransactionResponse(

                transaction.getId(),

                transaction.getType(),

                transaction.getAmount(),

                transaction.getReferenceId(),

                transaction.getDescription(),

                transaction.getOccurredAt()

        );

    }

}