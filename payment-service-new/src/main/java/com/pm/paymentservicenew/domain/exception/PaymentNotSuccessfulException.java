package com.pm.paymentservicenew.domain.exception;

public class PaymentNotSuccessfulException extends RuntimeException {

    public PaymentNotSuccessfulException() {

        super(

                "Payment was not successful."

        );

    }

}