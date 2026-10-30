package com.pm.paymentservicenew.domain.exception;

public class InvalidEscrowStateException extends RuntimeException {

    public InvalidEscrowStateException(String message) {
        super(message);
    }

}