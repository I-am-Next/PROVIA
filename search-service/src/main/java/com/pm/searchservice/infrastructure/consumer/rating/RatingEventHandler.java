
package com.pm.searchservice.infrastructure.consumer.rating;

import com.pm.contracts.rating.CustomerRatedEvent;
import com.pm.contracts.rating.CustomerRatingDeletedEvent;
import com.pm.contracts.rating.CustomerRatingUpdatedEvent;
import com.pm.searchservice.domain.model.ProviderSearchDocument;
import com.pm.searchservice.domain.ports.out.ProviderSearchRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class RatingEventHandler {

    private final ProviderSearchRepository providerRepository;

    public RatingEventHandler(
            ProviderSearchRepository providerRepository
    ) {
        this.providerRepository = providerRepository;
    }

    // =========================================================
    // CUSTOMER RATED
    // =========================================================

    public void handle(CustomerRatedEvent event) {
        updateRating(
                event.providerId(),
                event.averageRating(),
                event.totalRatings()
        );
    }

    // =========================================================
    // CUSTOMER RATING UPDATED
    // =========================================================

    public void handle(CustomerRatingUpdatedEvent event) {
        updateRating(
                event.providerId(),
                event.averageRating(),
                event.totalRatings()
        );
    }

    // =========================================================
    // CUSTOMER RATING DELETED
    // =========================================================

    public void handle(CustomerRatingDeletedEvent event) {
        updateRating(
                event.providerId(),
                event.averageRating(),
                event.totalRatings()
        );
    }

    // =========================================================
    // UPDATE PROVIDER RATING PROJECTION
    // =========================================================

    private void updateRating(
            UUID providerId,
            Double averageRating,
            Long totalRatings
    ) {

        ProviderSearchDocument existing =
                providerRepository
                        .findByProviderId(providerId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Provider search document not found: "
                                                + providerId
                                )
                        );

        BigDecimal rating =
                averageRating == null
                        ? null
                        : BigDecimal.valueOf(averageRating);

        int reviewCount =
                totalRatings == null
                        ? 0
                        : Math.toIntExact(totalRatings);

        ProviderSearchDocument updated =
                new ProviderSearchDocument(
                        existing.getProviderId(),
                        existing.getProviderName(),
                        existing.getDescription(),
                        existing.getLocation(),
                        rating,
                        reviewCount,
                        existing.isVerified(),
                        existing.getProfilePhotoMediaId(),
                        existing.getServices()
                );

        providerRepository.save(updated);
    }
}
