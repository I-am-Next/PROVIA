
package com.pm.searchservice.api.dto.response;

import com.pm.searchservice.domain.model.ProviderSearchDocument;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProviderSearchResponse(
        UUID providerId,
        String providerName,
        String description,
        String location,
        BigDecimal rating,
        int reviewCount,
        boolean verified,
        UUID profilePhotoMediaId,
        BigDecimal startingPrice,
        List<ServiceResponse> services
) {

    public record ServiceResponse(
            UUID providerServiceId,
            UUID serviceItemId,
            String serviceName,
            UUID categoryId,
            String categoryName,
            BigDecimal price,
            boolean active
    ) {
    }

    public static ProviderSearchResponse from(
            ProviderSearchDocument document
    ) {

        List<ServiceResponse> services =
                document.getServices()
                        .stream()
                        .map(service ->
                                new ServiceResponse(
                                        service.providerServiceId(),
                                        service.serviceItemId(),
                                        service.serviceName(),
                                        service.categoryId(),
                                        service.categoryName(),
                                        service.price(),
                                        service.active()
                                )
                        )
                        .toList();

        return new ProviderSearchResponse(
                document.getProviderId(),
                document.getProviderName(),
                document.getDescription(),
                document.getLocation(),
                document.getRating(),
                document.getReviewCount(),
                document.isVerified(),
                document.getProfilePhotoMediaId(),
                document.getStartingPrice(),
                services
        );
    }
}
