package com.pm.paymentservicenew.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletResponse(

        UUID walletId,

        UUID providerId,

        BigDecimal availableBalance,

        BigDecimal pendingBalance,

        BigDecimal reservedBalance,

        BigDecimal totalBalance

) {
}