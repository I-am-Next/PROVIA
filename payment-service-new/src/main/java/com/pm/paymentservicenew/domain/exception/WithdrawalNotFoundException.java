package com.pm.paymentservicenew.domain.exception;

import java.util.UUID;

public class WithdrawalNotFoundException extends RuntimeException {

    public WithdrawalNotFoundException(
            UUID id,
            String lookupType
    ) {

        super(

                String.format(

                        "Withdrawal not found for %s: %s.",

                        lookupType,

                        id

                )

        );

    }

}