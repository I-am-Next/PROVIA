package com.pm.paymentservicenew.integration.paystack.security;

import com.pm.paymentservicenew.domain.exception.InvalidWebhookSignatureException;
import com.pm.paymentservicenew.integration.paystack.config.PaystackProperties;
import org.springframework.stereotype.Component;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
public class PaystackWebhookVerifierImpl implements PaystackWebhookVerifier {

    private final PaystackProperties properties;

    public PaystackWebhookVerifierImpl(PaystackProperties properties) {

        this.properties = properties;
    }

    @Override
    public void verify(String payload, String signature) {


        String expectedSignature = computeSignature(

                payload

        );

        if (!expectedSignature.equalsIgnoreCase(

                signature

        )) {

            throw new InvalidWebhookSignatureException();

        }

    }

    private String computeSignature(

            String payload

    ) {

        try {

            Mac mac = Mac.getInstance(

                    "HmacSHA512"

            );

            SecretKeySpec secretKey =

                    new SecretKeySpec(

                            properties.secretKey().getBytes(

                                    StandardCharsets.UTF_8

                            ),

                            "HmacSHA512"

                    );

            mac.init(

                    secretKey

            );

            byte[] hash = mac.doFinal(

                    payload.getBytes(

                            StandardCharsets.UTF_8

                    )

            );

            return toHex(

                    hash

            );

        } catch (Exception ex) {

            throw new IllegalStateException(

                    "Unable to verify Paystack webhook signature.",

                    ex

            );

        }

    }

    private String toHex(

            byte[] bytes

    ) {

        StringBuilder builder = new StringBuilder();

        for (byte b : bytes) {

            builder.append(

                    String.format(

                            "%02x",

                            b

                    )

            );

        }

        return builder.toString();

    }

}