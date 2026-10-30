
package com.pm.searchservice.infrastructure.consumer.providerservice;

import com.pm.contracts.provider.ProviderServiceCreatedEvent;
import com.pm.contracts.provider.ProviderServiceDeletedEvent;
import com.pm.contracts.provider.ProviderServiceUpdatedEvent;
import com.pm.searchservice.domain.model.ProviderSearchDocument;
import com.pm.searchservice.domain.ports.out.ProviderSearchRepository;
import com.pm.searchservice.infrastructure.elasticsearch.CatalogServiceSearchDocumentEntity;
import com.pm.searchservice.infrastructure.elasticsearch.CatalogServiceSearchRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ProviderServiceEventHandler {

    private final ProviderSearchRepository providerRepository;
    private final CatalogServiceSearchRepository catalogServiceRepository;

    public ProviderServiceEventHandler(
            ProviderSearchRepository providerRepository,
            CatalogServiceSearchRepository catalogServiceRepository
    ) {
        this.providerRepository = providerRepository;
        this.catalogServiceRepository = catalogServiceRepository;
    }

    // =========================================================
    // CREATED
    // =========================================================

    public void handle(ProviderServiceCreatedEvent event) {

        ProviderSearchDocument provider =
                getProvider(event.providerId());

        CatalogServiceSearchDocumentEntity catalogService =
                getCatalogService(event.serviceItemId());

        List<ProviderSearchDocument.ServiceSearchItem> services =
                getServices(provider);

        removeExistingService(
                services,
                event.providerServiceId()
        );

        services.add(
                createServiceSearchItem(
                        event.providerServiceId(),
                        event.serviceItemId(),
                        event.price(),
                        event.active(),
                        catalogService
                )
        );

        saveProvider(provider, services);
    }

    // =========================================================
    // UPDATED
    // =========================================================

    public void handle(ProviderServiceUpdatedEvent event) {

        ProviderSearchDocument provider =
                getProvider(event.providerId());

        CatalogServiceSearchDocumentEntity catalogService =
                getCatalogService(event.serviceItemId());

        List<ProviderSearchDocument.ServiceSearchItem> services =
                getServices(provider);

        removeExistingService(
                services,
                event.providerServiceId()
        );

        /*
         * Keep inactive services in the projection.
         *
         * The active state is part of the read model.
         * Search filtering determines whether inactive services
         * are visible to customers.
         */
        services.add(
                createServiceSearchItem(
                        event.providerServiceId(),
                        event.serviceItemId(),
                        event.price(),
                        event.active(),
                        catalogService
                )
        );

        saveProvider(provider, services);
    }

    // =========================================================
    // DELETED
    // =========================================================

    public void handle(ProviderServiceDeletedEvent event) {

        ProviderSearchDocument provider =
                getProvider(event.providerId());

        List<ProviderSearchDocument.ServiceSearchItem> services =
                getServices(provider);

        removeExistingService(
                services,
                event.providerServiceId()
        );

        saveProvider(provider, services);
    }

    // =========================================================
    // GET PROVIDER
    // =========================================================

    private ProviderSearchDocument getProvider(UUID providerId) {

        return providerRepository
                .findByProviderId(providerId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Provider search document not found: "
                                        + providerId
                        )
                );
    }

    // =========================================================
    // GET CATALOG SERVICE
    // =========================================================

    private CatalogServiceSearchDocumentEntity getCatalogService(
            UUID serviceItemId
    ) {

        return catalogServiceRepository
                .findById(serviceItemId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Catalog service projection not found: "
                                        + serviceItemId
                        )
                );
    }

    // =========================================================
    // GET SERVICES
    // =========================================================

    private List<ProviderSearchDocument.ServiceSearchItem> getServices(
            ProviderSearchDocument provider
    ) {

        return provider.getServices() == null
                ? new ArrayList<>()
                : new ArrayList<>(provider.getServices());
    }

    // =========================================================
    // REMOVE EXISTING SERVICE
    // =========================================================

    private void removeExistingService(
            List<ProviderSearchDocument.ServiceSearchItem> services,
            UUID providerServiceId
    ) {

        services.removeIf(service ->
                service.providerServiceId().equals(providerServiceId)
        );
    }

    // =========================================================
    // CREATE SERVICE SEARCH ITEM
    // =========================================================

    private ProviderSearchDocument.ServiceSearchItem createServiceSearchItem(
            UUID providerServiceId,
            UUID serviceItemId,
            BigDecimal price,
            boolean active,
            CatalogServiceSearchDocumentEntity catalogService
    ) {

        return new ProviderSearchDocument.ServiceSearchItem(
                providerServiceId,
                serviceItemId,
                catalogService.getServiceName(),
                catalogService.getCategoryId(),
                catalogService.getCategoryName(),
                price,
                active
        );
    }

    // =========================================================
    // SAVE PROVIDER
    // =========================================================

    private void saveProvider(
            ProviderSearchDocument provider,
            List<ProviderSearchDocument.ServiceSearchItem> services
    ) {

        ProviderSearchDocument updatedProvider =
                new ProviderSearchDocument(
                        provider.getProviderId(),
                        provider.getProviderName(),
                        provider.getDescription(),
                        provider.getLocation(),
                        provider.getRating(),
                        provider.getReviewCount(),
                        provider.isVerified(),
                        provider.getProfilePhotoMediaId(),
                        services
                );

        providerRepository.save(updatedProvider);
    }
}
