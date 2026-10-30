package com.pm.paymentservicenew.api.controller;

import com.pm.paymentservicenew.application.service.PaymentWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class PaymentWebhookController {

    private final PaymentWebhookService paymentWebhookService;

    public PaymentWebhookController(

            PaymentWebhookService paymentWebhookService

    ) {

        this.paymentWebhookService = paymentWebhookService;

    }

    @PostMapping("/paystack")
    public ResponseEntity<Void> handlePaystackWebhook(
            @RequestBody String payload,
            @RequestHeader("x-paystack-signature") String signature

    ) {
        paymentWebhookService.handleWebhook(

                payload,
                signature
        );
        return ResponseEntity.ok().build();

    }

}