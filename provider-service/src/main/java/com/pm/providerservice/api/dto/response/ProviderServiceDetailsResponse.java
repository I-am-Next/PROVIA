package com.pm.providerservice.api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProviderServiceDetailsResponse(

        UUID id,

        UUID providerProfileId,

        UUID providerAuthUserId,

        UUID serviceItemId,

        String serviceName,

        String serviceDescription,

        String categoryName,

        BigDecimal price,

        boolean active

) {
}