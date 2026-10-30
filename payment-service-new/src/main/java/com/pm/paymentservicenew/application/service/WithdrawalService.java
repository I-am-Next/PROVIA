
package com.pm.paymentservicenew.application.service;

import com.pm.paymentservicenew.application.dto.request.CreateWithdrawalRequest;
import com.pm.paymentservicenew.application.dto.response.WithdrawalResponse;

import java.util.List;
import java.util.UUID;

public interface WithdrawalService {

    WithdrawalResponse requestWithdrawal(
            UUID providerId,
            CreateWithdrawalRequest request
    );

    WithdrawalResponse getWithdrawal(
            UUID providerId,
            UUID withdrawalId
    );

    List<WithdrawalResponse> getWithdrawals(
            UUID providerId
    );

    /**
     * Moves a pending withdrawal into processing.
     *
     * This should be invoked by a trusted payout-processing
     * workflow, not directly by a provider.
     */
    WithdrawalResponse startProcessingWithdrawal(
            UUID withdrawalId
    );

    WithdrawalResponse completeWithdrawal(
            UUID withdrawalId
    );

    void failWithdrawal(
            UUID withdrawalId,
            String reason
    );
}
