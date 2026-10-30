package com.pm.paymentservicenew.domain.exception;

public class InvalidWithdrawalStateException extends RuntimeException {

    public InvalidWithdrawalStateException(String message) {
        super(message);
    }

}