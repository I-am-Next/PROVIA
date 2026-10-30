package com.pm.paymentservicenew.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.paymentservicenew.domain.exception.DuplicateWebhookEventException;
import com.pm.paymentservicenew.domain.exception.InvalidWebhookPayloadException;
import com.pm.paymentservicenew.integration.paystack.dto.PaystackWebhookEvent;
import com.pm.paymentservicenew.integration.paystack.service.PaymentGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentWebhookServiceImpl implements PaymentWebhookService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentWebhookServiceImpl.class);

    private final PaymentGatewayService paymentGatewayService;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    private final IdempotencyService idempotencyService;

    public PaymentWebhookServiceImpl(
            PaymentGatewayService paymentGatewayService,
            PaymentService paymentService,
            ObjectMapper objectMapper,
            IdempotencyService idempotencyService
    ) {
        this.paymentGatewayService = paymentGatewayService;
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
        this.idempotencyService = idempotencyService;
    }

    @Override
    public void handleWebhook(
            String payload,
            String signature
    ) {

        log.info("========== PAYSTACK WEBHOOK ==========");

        /*
         * Verify webhook authenticity.
         */
        paymentGatewayService.verifyWebhookSignature(
                payload,
                signature
        );

        log.info("Webhook signature verified.");

        PaystackWebhookEvent event;

        try {

            event = objectMapper.readValue(
                    payload,
                    PaystackWebhookEvent.class
            );

        } catch (Exception ex) {

            throw new InvalidWebhookPayloadException(ex);

        }

        log.info("Received event: {}", event.event());

        /*
         * Ignore events we don't care about.
         */
        if (!"charge.success".equals(event.event())) {

            log.info(
                    "Ignoring event '{}'.",
                    event.event()
            );

            return;
        }

        String reference = event.data().reference();

        try {

            idempotencyService.registerEvent(
                    "PAYSTACK",
                    reference,
                    event.event()
            );

            /*
             * Verify the payment exactly once.
             *
             * The idempotency record guarantees that
             * duplicate Paystack webhook deliveries do not
             * trigger payment processing again.
             */
            paymentService.verifyPayment(reference);

        } catch (DuplicateWebhookEventException ex) {

            log.info(
                    "Webhook already processed. Reference={}",
                    reference
            );

            return;
        }

        log.info(
                "Webhook processed successfully. Reference={}",
                reference
        );

        log.info("======================================");
    }
}