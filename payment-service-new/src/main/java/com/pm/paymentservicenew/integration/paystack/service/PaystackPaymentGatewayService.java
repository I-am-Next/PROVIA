package com.pm.paymentservicenew.integration.paystack.service;

import com.pm.paymentservicenew.domain.exception.InvalidWebhookSignatureException;
import com.pm.paymentservicenew.domain.exception.PaymentNotSuccessfulException;
import com.pm.paymentservicenew.domain.exception.PaymentVerificationFailedException;
import com.pm.paymentservicenew.integration.paystack.client.PaystackClient;
import com.pm.paymentservicenew.integration.paystack.config.PaystackProperties;
import com.pm.paymentservicenew.integration.paystack.dto.InitializePaymentResult;
import com.pm.paymentservicenew.integration.paystack.dto.InitializeTransactionRequest;
import com.pm.paymentservicenew.integration.paystack.dto.InitializeTransactionResponse;
import com.pm.paymentservicenew.integration.paystack.dto.PaystackResponse;
import com.pm.paymentservicenew.integration.paystack.dto.VerifyTransactionResponse;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@Service
public class PaystackPaymentGatewayService implements PaymentGatewayService {

    private final PaystackClient paystackClient;
    private final PaystackProperties properties;

    public PaystackPaymentGatewayService(
            PaystackClient paystackClient,
            PaystackProperties properties
    ) {
        this.paystackClient = paystackClient;
        this.properties = properties;
    }

    @Override
    public InitializePaymentResult initializePayment(
            UUID paymentId,
            String email,
            BigDecimal amount
    ) {
        String reference = paymentId.toString();

        InitializeTransactionRequest request =
                new InitializeTransactionRequest(
                        email,
                        toKobo(amount),
                        reference,
                        properties.callbackUrl()
                );

        PaystackResponse<InitializeTransactionResponse> response =
                paystackClient.initializeTransaction(request);

        return new InitializePaymentResult(
                response.data().authorization_url(),
                response.data().reference()
        );
    }

    @Override
    public VerifyTransactionResponse verifyPayment(String reference) {

        PaystackResponse<VerifyTransactionResponse> response =
                paystackClient.verifyTransaction(reference);

        if (!response.status()) {
            throw new PaymentVerificationFailedException();
        }

        VerifyTransactionResponse verification = response.data();

        if (!"success".equalsIgnoreCase(verification.status())) {
            throw new PaymentNotSuccessfulException();
        }

        return verification;
    }

    @Override
    public void verifyWebhookSignature(
            String payload,
            String signature
    ) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");

            SecretKeySpec secretKey = new SecretKeySpec(
                    properties.secretKey().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA512"
            );

            mac.init(secretKey);

            byte[] hash = mac.doFinal(
                    payload.getBytes(StandardCharsets.UTF_8)
            );

            String expectedSignature = bytesToHex(hash);

            boolean valid = MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );

            if (!valid) {
                throw new InvalidWebhookSignatureException();
            }

        } catch (InvalidWebhookSignatureException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new InvalidWebhookSignatureException(ex);
        }
    }

    private long toKobo(BigDecimal amount) {
        return amount
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);

        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }

        return builder.toString();
    }
}