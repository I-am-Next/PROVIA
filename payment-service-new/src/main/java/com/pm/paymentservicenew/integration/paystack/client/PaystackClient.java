package com.pm.paymentservicenew.integration.paystack.client;

import com.pm.paymentservicenew.integration.paystack.dto.InitializeTransactionRequest;
import com.pm.paymentservicenew.integration.paystack.dto.InitializeTransactionResponse;
import com.pm.paymentservicenew.integration.paystack.dto.PaystackResponse;
import com.pm.paymentservicenew.integration.paystack.dto.VerifyTransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaystackClient {

    private static final Logger log =
            LoggerFactory.getLogger(PaystackClient.class);

    private final RestClient restClient;

    public PaystackClient(RestClient paystackRestClient) {
        this.restClient = paystackRestClient;
    }

    public PaystackResponse<InitializeTransactionResponse> initializeTransaction(
            InitializeTransactionRequest request
    ) {

        log.info("Calling POST /transaction/initialize");
        log.info("Request = {}", request);

        try {

            return restClient.post()
                    .uri("/transaction/initialize")
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

        } catch (Exception ex) {

            log.error("Failed while calling Paystack initialize endpoint.", ex);

            throw ex;
        }

    }

    public PaystackResponse<VerifyTransactionResponse> verifyTransaction(
            String reference
    ) {

        log.info("Calling GET /transaction/verify/{}", reference);

        try {

            return restClient.get()
                    .uri("/transaction/verify/{reference}", reference)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

        } catch (Exception ex) {

            log.error("Failed while calling Paystack verify endpoint.", ex);

            throw ex;
        }

    }

}