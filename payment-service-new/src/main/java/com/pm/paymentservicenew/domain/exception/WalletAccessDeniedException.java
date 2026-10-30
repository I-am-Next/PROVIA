package com.pm.paymentservicenew.domain.exception;

public class WalletAccessDeniedException extends RuntimeException {

    public WalletAccessDeniedException(String message) {
        super(message);
    }

}