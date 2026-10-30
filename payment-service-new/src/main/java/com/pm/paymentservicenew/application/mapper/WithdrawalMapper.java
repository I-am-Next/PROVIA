package com.pm.paymentservicenew.application.mapper;

import com.pm.paymentservicenew.application.dto.response.WithdrawalResponse;
import com.pm.paymentservicenew.domain.model.withdrawal.Withdrawal;
import org.springframework.stereotype.Component;

@Component
public record WithdrawalMapper() {

    public WithdrawalResponse toResponse(
            Withdrawal withdrawal
    ) {

        return new WithdrawalResponse(

                withdrawal.getId(),

                withdrawal.getAmount(),

                withdrawal.getStatus(),

                withdrawal.getRequestedAt()

        );

    }

}