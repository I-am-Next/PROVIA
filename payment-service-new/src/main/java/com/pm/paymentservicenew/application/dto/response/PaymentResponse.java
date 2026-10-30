package com.pm.paymentservicenew.application.dto.response;

import com.pm.paymentservicenew.domain.model.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(

        UUID paymentId,

        UUID bookingId,

        BigDecimal amount,

        PaymentStatus status,

        Instant createdAt


) {
}