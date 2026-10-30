package com.pm.paymentservicenew.domain.exception;

public class PaymentAccessDeniedException extends RuntimeException {

    public PaymentAccessDeniedException(String message) {
        super(message);
    }

}