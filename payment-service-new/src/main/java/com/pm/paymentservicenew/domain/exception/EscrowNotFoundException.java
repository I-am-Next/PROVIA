package com.pm.paymentservicenew.domain.exception;

import java.util.UUID;

public class EscrowNotFoundException extends RuntimeException {

    public EscrowNotFoundException(
            UUID id,
            String lookupType
    ) {

        super(

                String.format(

                        "Escrow not found for %s: %s.",

                        lookupType,

                        id

                )

        );

    }

}