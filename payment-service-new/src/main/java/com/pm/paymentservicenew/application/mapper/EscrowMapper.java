package com.pm.paymentservicenew.application.mapper;

import com.pm.paymentservicenew.application.dto.response.EscrowResponse;
import com.pm.paymentservicenew.domain.model.escrow.Escrow;

import java.math.BigDecimal;

public final class EscrowMapper {

    private EscrowMapper() {
    }

    public static EscrowResponse toResponse(Escrow escrow) {

        return new EscrowResponse(

                escrow.getId(),

                escrow.getBookingId(),

                escrow.getTotalAmount(),

                escrow.getReleasedAmount(),

                escrow.getTotalAmount().subtract(
                        escrow.getReleasedAmount()
                ),

                escrow.getStatus()

        );
    }

}