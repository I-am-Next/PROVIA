package com.pm.providerservice.api.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProviderServiceResponse(

        UUID id,

        UUID providerProfileId,

        UUID serviceItemId,

        String serviceName,

        String serviceDescription,

        String categoryName,

        BigDecimal price,

        boolean active,

        Instant createdAt

) {
}