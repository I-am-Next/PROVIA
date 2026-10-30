
package com.pm.searchservice.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProviderSearchDocument {

    private final UUID providerId;
    private final String providerName;
    private final String description;
    private final String location;
    private final BigDecimal rating;
    private final int reviewCount;
    private final boolean verified;
    private final UUID profilePhotoMediaId;
    private final List<ServiceSearchItem> services;

    public ProviderSearchDocument(
            UUID providerId,
            String providerName,
            String description,
            String location,
            BigDecimal rating,
            int reviewCount,
            boolean verified,
            UUID profilePhotoMediaId,
            List<ServiceSearchItem> services
    ) {
        this.providerId = providerId;
        this.providerName = providerName;
        this.description = description;
        this.location = location;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.verified = verified;
        this.profilePhotoMediaId = profilePhotoMediaId;
        this.services = services == null
                ? List.of()
                : List.copyOf(services);
    }

    public UUID getProviderId() {
        return providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public boolean isVerified() {
        return verified;
    }

    public UUID getProfilePhotoMediaId() {
        return profilePhotoMediaId;
    }

    public List<ServiceSearchItem> getServices() {
        return services;
    }

    /**
     * Returns the lowest price among active provider services.
     *
     * This is the provider's starting price for marketplace search.
     */
    public BigDecimal getStartingPrice() {
        return services.stream()
                .filter(ServiceSearchItem::active)
                .map(ServiceSearchItem::price)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(null);
    }

    public record ServiceSearchItem(
            UUID providerServiceId,
            UUID serviceItemId,
            String serviceName,
            UUID categoryId,
            String categoryName,
            BigDecimal price,
            boolean active
    ) {
    }
}
