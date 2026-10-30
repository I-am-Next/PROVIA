package com.pm.paymentservicenew.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.paymentservicenew.domain.exception.DuplicateWebhookEventException;
import com.pm.paymentservicenew.domain.exception.InvalidWebhookPayloadException;
import com.pm.paymentservicenew.integration.paystack.service.PaymentGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookServiceImplTest {

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private IdempotencyService idempotencyService;

    private PaymentWebhookServiceImpl webhookService;

    @BeforeEach
    void setUp() {

        ObjectMapper objectMapper = new ObjectMapper();

        webhookService = new PaymentWebhookServiceImpl(
                paymentGatewayService,
                paymentService,
                objectMapper,
                idempotencyService
        );
    }

    @Test
    void shouldVerifyAndProcessSuccessfulChargeExactlyOnce() {

        String reference =
                "550e8400-e29b-41d4-a716-446655440000";

        String payload = """
                {
                  "event": "charge.success",
                  "data": {
                    "reference": "%s"
                  }
                }
                """.formatted(reference);

        webhookService.handleWebhook(
                payload,
                "valid-signature"
        );

        verify(paymentGatewayService)
                .verifyWebhookSignature(
                        payload,
                        "valid-signature"
                );

        verify(idempotencyService)
                .registerEvent(
                        "PAYSTACK",
                        reference,
                        "charge.success"
                );

        verify(paymentService, times(1))
                .verifyPayment(reference);
    }

    @Test
    void shouldIgnoreDuplicateWebhook() {

        String reference =
                "550e8400-e29b-41d4-a716-446655440000";

        String payload = """
                {
                  "event": "charge.success",
                  "data": {
                    "reference": "%s"
                  }
                }
                """.formatted(reference);

        doThrow(new DuplicateWebhookEventException())
                .when(idempotencyService)
                .registerEvent(
                        "PAYSTACK",
                        reference,
                        "charge.success"
                );

        webhookService.handleWebhook(
                payload,
                "valid-signature"
        );

        verify(idempotencyService)
                .registerEvent(
                        "PAYSTACK",
                        reference,
                        "charge.success"
                );

        verify(paymentService, never())
                .verifyPayment(anyString());
    }

    @Test
    void shouldIgnoreUnsupportedEvent() {

        String reference =
                "550e8400-e29b-41d4-a716-446655440000";

        String payload = """
                {
                  "event": "charge.failed",
                  "data": {
                    "reference": "%s"
                  }
                }
                """.formatted(reference);

        webhookService.handleWebhook(
                payload,
                "valid-signature"
        );

        verify(paymentGatewayService)
                .verifyWebhookSignature(
                        payload,
                        "valid-signature"
                );

        verify(idempotencyService, never())
                .registerEvent(
                        anyString(),
                        anyString(),
                        anyString()
                );

        verify(paymentService, never())
                .verifyPayment(anyString());
    }

    @Test
    void shouldRejectInvalidWebhookPayload() {

        String payload = """
                {
                  "event": "charge.success",
                  "data":
                """;

        assertThrows(
                InvalidWebhookPayloadException.class,
                () -> webhookService.handleWebhook(
                        payload,
                        "valid-signature"
                )
        );

        verify(paymentGatewayService)
                .verifyWebhookSignature(
                        payload,
                        "valid-signature"
                );

        verifyNoInteractions(idempotencyService);

        verifyNoInteractions(paymentService);
    }

    @Test
    void shouldNotProcessWebhookWhenSignatureVerificationFails() {

        String payload = """
                {
                  "event": "charge.success",
                  "data": {
                    "reference":
                        "550e8400-e29b-41d4-a716-446655440000"
                  }
                }
                """;

        RuntimeException signatureException =
                new RuntimeException("Invalid webhook signature");

        doThrow(signatureException)
                .when(paymentGatewayService)
                .verifyWebhookSignature(
                        payload,
                        "invalid-signature"
                );

        assertThrows(
                RuntimeException.class,
                () -> webhookService.handleWebhook(
                        payload,
                        "invalid-signature"
                )
        );

        verify(paymentGatewayService)
                .verifyWebhookSignature(
                        payload,
                        "invalid-signature"
                );

        verifyNoInteractions(idempotencyService);

        verifyNoInteractions(paymentService);
    }
}