package com.pm.paymentservicenew.application.dto.response;

import com.pm.paymentservicenew.domain.model.wallet.WalletTransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletTransactionResponse(

        UUID transactionId,

        WalletTransactionType type,

        BigDecimal amount,

        UUID referenceId,

        String description,

        Instant occurredAt

) {
}