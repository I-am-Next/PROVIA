package com.pm.paymentservicenew.application.service;

import com.pm.paymentservicenew.application.dto.request.CreatePaymentRequest;
import com.pm.paymentservicenew.application.dto.response.PaymentInitializationResponse;
import com.pm.paymentservicenew.application.dto.response.PaymentResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    /*
     * Payment initialization
     */
    PaymentInitializationResponse createPayment(
            CreatePaymentRequest request,
            UUID customerId
    );

    /*
     * Verify payment with Paystack.
     * Called by webhook processing and can also be reused
     * by a manual verification endpoint.
     */
    void verifyPayment(
            String reference
    );

    /*
     * Queries
     */

    PaymentResponse getPayment(
            UUID customerId,
            UUID paymentId
    );

    PaymentResponse getPaymentByBooking(
            UUID customerId,
            UUID bookingId
    );

    List<PaymentResponse> getCustomerPayments(
            UUID customerId
    );

}