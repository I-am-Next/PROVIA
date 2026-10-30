package com.pm.paymentservicenew.application.dto.response;

import com.pm.paymentservicenew.domain.model.withdrawal.WithdrawalStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WithdrawalResponse(

        UUID withdrawalId,

        BigDecimal amount,

        WithdrawalStatus status,

        Instant requestedAt

) {
}