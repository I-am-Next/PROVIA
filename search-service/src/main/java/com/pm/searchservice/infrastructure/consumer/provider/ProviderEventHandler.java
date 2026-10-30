
package com.pm.searchservice.infrastructure.consumer.provider;

import com.pm.contracts.provider.ProviderCreatedEvent;
import com.pm.contracts.provider.ProviderDeletedEvent;
import com.pm.contracts.provider.ProviderUpdatedEvent;
import com.pm.searchservice.domain.model.ProviderSearchDocument;
import com.pm.searchservice.domain.ports.out.ProviderSearchRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProviderEventHandler {

    private final ProviderSearchRepository providerRepository;

    public ProviderEventHandler(
            ProviderSearchRepository providerRepository
    ) {
        this.providerRepository = providerRepository;
    }

    // =========================================================
    // CREATED
    // =========================================================

    public void handle(ProviderCreatedEvent event) {

        ProviderSearchDocument document =
                new ProviderSearchDocument(
                        event.providerId(),
                        event.businessName(),
                        event.description(),
                        null,
                        null,
                        0,
                        event.verified(),
                        event.profilePhotoMediaId(),
                        List.of()
                );

        providerRepository.save(document);
    }

    // =========================================================
    // UPDATED
    // =========================================================

    public void handle(ProviderUpdatedEvent event) {

        ProviderSearchDocument existing =
                providerRepository
                        .findByProviderId(event.providerId())
                        .orElse(null);

        /*
         * If the provider document does not exist yet, create a
         * baseline projection from the update event.
         *
         * This can happen if events arrive out of order.
         */
        if (existing == null) {

            ProviderSearchDocument document =
                    new ProviderSearchDocument(
                            event.providerId(),
                            event.businessName(),
                            event.description(),
                            null,
                            null,
                            0,
                            event.verified(),
                            event.profilePhotoMediaId(),
                            List.of()
                    );

            providerRepository.save(document);
            return;
        }

        /*
         * ProviderUpdatedEvent owns provider information,
         * including the profile photo media reference.
         *
         * Preserve fields owned by other projections:
         * - location
         * - ratings and review count
         * - services
         */
        ProviderSearchDocument updated =
                new ProviderSearchDocument(
                        existing.getProviderId(),
                        event.businessName(),
                        event.description(),
                        existing.getLocation(),
                        existing.getRating(),
                        existing.getReviewCount(),
                        event.verified(),
                        event.profilePhotoMediaId(),
                        existing.getServices()
                );

        providerRepository.save(updated);
    }

    // =========================================================
    // DELETED
    // =========================================================

    public void handle(ProviderDeletedEvent event) {

        providerRepository.deleteByProviderId(
                event.providerId()
        );
    }
}
