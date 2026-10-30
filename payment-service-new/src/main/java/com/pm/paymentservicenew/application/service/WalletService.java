
package com.pm.paymentservicenew.application.service;

import com.pm.paymentservicenew.application.dto.response.WalletResponse;
import com.pm.paymentservicenew.application.dto.response.WalletTransactionResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface WalletService {

    WalletResponse getWallet(UUID providerId);

    List<WalletTransactionResponse> getTransactions(UUID providerId);

    /**
     * Credits the provider for a specific financial operation.
     *
     * operationId must remain stable across retries.
     * escrowId identifies the escrow for event publishing.
     */
    void creditWallet(
            UUID providerId,
            UUID operationId,
            UUID escrowId,
            BigDecimal amount,
            String description
    );

    void reserveFunds(
            UUID providerId,
            BigDecimal amount,
            UUID withdrawalId,
            String description
    );

    void completeWithdrawal(
            UUID providerId,
            UUID withdrawalId,
            BigDecimal amount,
            String description
    );

    void cancelWithdrawal(
            UUID providerId,
            UUID withdrawalId,
            BigDecimal amount,
            String description
    );
}
