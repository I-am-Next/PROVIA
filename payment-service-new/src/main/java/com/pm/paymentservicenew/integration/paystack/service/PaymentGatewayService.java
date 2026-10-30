package com.pm.paymentservicenew.integration.paystack.service;

import com.pm.paymentservicenew.integration.paystack.dto.InitializePaymentResult;
import com.pm.paymentservicenew.integration.paystack.dto.VerifyTransactionResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentGatewayService {

    InitializePaymentResult initializePayment(
            UUID paymentId,
            String email,
            BigDecimal amount
    );

    VerifyTransactionResponse verifyPayment(
            String reference
    );

    void verifyWebhookSignature(
            String payload,
            String signature
    );

}