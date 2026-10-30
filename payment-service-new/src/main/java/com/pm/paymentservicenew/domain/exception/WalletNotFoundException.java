package com.pm.paymentservicenew.domain.exception;

import java.util.UUID;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(UUID providerId) {
        super("Wallet not found for provider: " + providerId);
    }

}