package com.pm.paymentservicenew.application.service;

import com.pm.paymentservicenew.application.dto.response.EscrowResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface EscrowService {

    EscrowResponse releaseMaterials(
            UUID bookingId,
            UUID quoteId,
            BigDecimal amount
    );


    EscrowResponse releaseLabour(UUID bookingId);


    void refundEscrow(UUID bookingId);
}
