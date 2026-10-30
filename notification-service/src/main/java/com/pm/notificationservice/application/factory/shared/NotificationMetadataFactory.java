package com.pm.notificationservice.application.factory.shared;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class NotificationMetadataFactory {

    private final ObjectMapper objectMapper;

    public NotificationMetadataFactory(

            ObjectMapper objectMapper

    ) {

        this.objectMapper = objectMapper;

    }

    /*
     * Booking
     */

    public String bookingMetadata(

            UUID bookingId

    ) {

        return bookingMetadata(

                bookingId,

                null

        );

    }

    public String bookingMetadata(

            UUID bookingId,

            String serviceName

    ) {

        Map<String, Object> metadata = new LinkedHashMap<>();

        metadata.put(

                "bookingId",

                bookingId

        );

        if (serviceName != null && !serviceName.isBlank()) {

            metadata.put(

                    "serviceName",

                    serviceName

            );

        }

        return toJson(

                metadata

        );

    }

    /*
     * Payment
     */

    public String paymentMetadata(

            UUID bookingId,

            UUID paymentId

    ) {

        return paymentMetadata(

                bookingId,

                paymentId,

                null,

                null

        );

    }

    public String paymentMetadata(

            UUID bookingId,

            UUID paymentId,

            BigDecimal amount,

            String currency

    ) {

        Map<String, Object> metadata = new LinkedHashMap<>();

        metadata.put(

                "bookingId",

                bookingId

        );

        metadata.put(

                "paymentId",

                paymentId

        );

        if (amount != null) {

            metadata.put(

                    "amount",

                    amount

            );

        }

        if (currency != null && !currency.isBlank()) {

            metadata.put(

                    "currency",

                    currency

            );

        }

        return toJson(

                metadata

        );

    }

    /*
     * Escrow
     */

    public String escrowMetadata(

            UUID bookingId,

            UUID paymentId,

            UUID escrowId

    ) {

        return escrowMetadata(

                bookingId,

                paymentId,

                escrowId,

                null,

                null,

                null

        );

    }

    public String escrowMetadata(

            UUID bookingId,

            UUID paymentId,

            UUID escrowId,

            BigDecimal amount

    ) {

        return escrowMetadata(

                bookingId,

                paymentId,

                escrowId,

                null,

                null,

                amount

        );

    }

    public String escrowMetadata(

            UUID bookingId,

            UUID paymentId,

            UUID escrowId,

            UUID customerId,

            UUID providerId,

            BigDecimal amount

    ) {

        Map<String, Object> metadata = new LinkedHashMap<>();

        metadata.put(

                "bookingId",

                bookingId

        );

        metadata.put(

                "paymentId",

                paymentId

        );

        metadata.put(

                "escrowId",

                escrowId

        );

        if (customerId != null) {

            metadata.put(

                    "customerId",

                    customerId

            );

        }

        if (providerId != null) {

            metadata.put(

                    "providerId",

                    providerId

            );

        }

        if (amount != null) {

            metadata.put(

                    "amount",

                    amount

            );

        }

        return toJson(

                metadata

        );

    }

    /*
     * Wallet
     */

    public String walletMetadata(

            UUID walletId,

            BigDecimal amount

    ) {

        Map<String, Object> metadata = new LinkedHashMap<>();

        metadata.put(

                "walletId",

                walletId

        );

        metadata.put(

                "amount",

                amount

        );

        return toJson(

                metadata

        );

    }

    public String withdrawalMetadata(

            UUID walletId,

            UUID withdrawalId,

            BigDecimal amount

    ) {

        return withdrawalMetadata(

                walletId,

                withdrawalId,

                amount,

                null

        );

    }

    public String withdrawalMetadata(

            UUID walletId,

            UUID withdrawalId,

            BigDecimal amount,

            String reason

    ) {

        Map<String, Object> metadata = new LinkedHashMap<>();

        metadata.put(

                "walletId",

                walletId

        );

        metadata.put(

                "withdrawalId",

                withdrawalId

        );

        metadata.put(

                "amount",

                amount

        );

        if (reason != null && !reason.isBlank()) {

            metadata.put(

                    "reason",

                    reason

            );

        }

        return toJson(

                metadata

        );

    }

    /*
     * Rating
     */

    public String ratingMetadata(

            UUID bookingId,

            UUID ratingId

    ) {

        return ratingMetadata(

                bookingId,

                ratingId,

                null

        );

    }

    public String ratingMetadata(

            UUID bookingId,

            UUID ratingId,

            Integer rating

    ) {

        Map<String, Object> metadata = new LinkedHashMap<>();

        metadata.put(

                "bookingId",

                bookingId

        );

        metadata.put(

                "ratingId",

                ratingId

        );

        if (rating != null) {

            metadata.put(

                    "rating",

                    rating

            );

        }

        return toJson(

                metadata

        );

    }

    /*
     * Chat
     */

    public String chatMetadata(

            UUID conversationId,

            UUID senderId

    ) {

        return chatMetadata(

                conversationId,

                senderId,

                null

        );

    }

    public String chatMetadata(

            UUID conversationId,

            UUID senderId,

            String senderName

    ) {

        Map<String, Object> metadata = new LinkedHashMap<>();

        metadata.put(

                "conversationId",

                conversationId

        );

        metadata.put(

                "senderId",

                senderId

        );

        if (senderName != null && !senderName.isBlank()) {

            metadata.put(

                    "senderName",

                    senderName

            );

        }

        return toJson(

                metadata

        );

    }

    /*
     * System
     */

    public String systemMetadata(

            String category

    ) {

        Map<String, Object> metadata = new LinkedHashMap<>();

        metadata.put(

                "category",

                category

        );

        return toJson(

                metadata

        );

    }

    /*
     * Admin
     */

    public String adminMetadata(

            String category

    ) {

        Map<String, Object> metadata = new LinkedHashMap<>();

        metadata.put(

                "category",

                category

        );

        return toJson(

                metadata

        );

    }

    /*
     * Shared
     */

    private String toJson(

            Map<String, Object> metadata

    ) {

        try {

            return objectMapper.writeValueAsString(

                    metadata

            );

        } catch (JsonProcessingException ex) {

            throw new IllegalStateException(

                    "Failed to serialize notification metadata.",

                    ex

            );

        }

    }

}