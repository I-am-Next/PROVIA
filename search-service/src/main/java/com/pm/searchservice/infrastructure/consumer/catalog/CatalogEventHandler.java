package com.pm.searchservice.infrastructure.consumer.catalog;

import com.pm.contracts.catalog.CategoryCreatedEvent;
import com.pm.contracts.catalog.CategoryDeletedEvent;
import com.pm.contracts.catalog.CategoryUpdatedEvent;
import com.pm.contracts.catalog.ServiceItemCreatedEvent;
import com.pm.contracts.catalog.ServiceItemDeletedEvent;
import com.pm.contracts.catalog.ServiceItemUpdatedEvent;

import com.pm.searchservice.domain.model.ProviderSearchDocument;
import com.pm.searchservice.domain.ports.out.ProviderSearchRepository;

import com.pm.searchservice.infrastructure.elasticsearch.CatalogCategorySearchDocumentEntity;
import com.pm.searchservice.infrastructure.elasticsearch.CatalogCategorySearchRepository;
import com.pm.searchservice.infrastructure.elasticsearch.CatalogServiceSearchDocumentEntity;
import com.pm.searchservice.infrastructure.elasticsearch.CatalogServiceSearchRepository;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class CatalogEventHandler {

    private final CatalogCategorySearchRepository categoryRepository;
    private final CatalogServiceSearchRepository serviceRepository;
    private final ProviderSearchRepository providerRepository;

    public CatalogEventHandler(
            CatalogCategorySearchRepository categoryRepository,
            CatalogServiceSearchRepository serviceRepository,
            ProviderSearchRepository providerRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.serviceRepository = serviceRepository;
        this.providerRepository = providerRepository;
    }

    // =========================================================
    // CATEGORY CREATED
    // =========================================================

    public void handle(CategoryCreatedEvent event) {

        CatalogCategorySearchDocumentEntity document =
                new CatalogCategorySearchDocumentEntity(
                        event.categoryId(),
                        event.name(),
                        event.description(),
                        event.active()
                );

        categoryRepository.save(document);
    }

    // =========================================================
    // CATEGORY UPDATED
    // =========================================================

    public void handle(CategoryUpdatedEvent event) {

        CatalogCategorySearchDocumentEntity document =
                new CatalogCategorySearchDocumentEntity(
                        event.categoryId(),
                        event.name(),
                        event.description(),
                        event.active()
                );

        categoryRepository.save(document);

        updateServiceCategoryName(event);

        updateProviderCategoryName(event);
    }

    // =========================================================
    // CATEGORY DELETED
    // =========================================================

    public void handle(CategoryDeletedEvent event) {

        categoryRepository.deleteById(
                event.categoryId()
        );
    }

    // =========================================================
    // SERVICE ITEM CREATED
    // =========================================================

    public void handle(ServiceItemCreatedEvent event) {

        String categoryName =
                getCategoryName(event.categoryId());

        CatalogServiceSearchDocumentEntity document =
                new CatalogServiceSearchDocumentEntity(
                        event.serviceItemId(),
                        event.name(),
                        event.description(),
                        event.categoryId(),
                        categoryName,
                        event.active()
                );

        serviceRepository.save(document);
    }

    // =========================================================
    // SERVICE ITEM UPDATED
    // =========================================================

    public void handle(ServiceItemUpdatedEvent event) {

        String categoryName =
                getCategoryName(event.categoryId());

        CatalogServiceSearchDocumentEntity document =
                new CatalogServiceSearchDocumentEntity(
                        event.serviceItemId(),
                        event.name(),
                        event.description(),
                        event.categoryId(),
                        categoryName,
                        event.active()
                );

        // Update the catalog search projection.
        serviceRepository.save(document);

        // Update provider search projections that reference this item.
        updateProviderServiceItem(event, categoryName);
    }

    // =========================================================
    // SERVICE ITEM DELETED
    // =========================================================

    public void handle(ServiceItemDeletedEvent event) {

        serviceRepository.deleteById(
                event.serviceItemId()
        );
    }

    // =========================================================
    // GET CATEGORY NAME
    // =========================================================

    private String getCategoryName(UUID categoryId) {

        return categoryRepository
                .findById(categoryId)
                .map(
                        CatalogCategorySearchDocumentEntity::getCategoryName
                )
                .orElse(null);
    }

    // =========================================================
    // UPDATE SERVICE CATEGORY NAME
    // =========================================================

    private void updateServiceCategoryName(
            CategoryUpdatedEvent event
    ) {

        List<CatalogServiceSearchDocumentEntity> services =
                serviceRepository.findByCategoryId(
                        event.categoryId()
                );

        for (CatalogServiceSearchDocumentEntity service : services) {

            CatalogServiceSearchDocumentEntity updated =
                    new CatalogServiceSearchDocumentEntity(
                            service.getServiceItemId(),
                            service.getServiceName(),
                            service.getDescription(),
                            service.getCategoryId(),
                            event.name(),
                            service.isActive()
                    );

            serviceRepository.save(updated);
        }
    }

    // =========================================================
    // UPDATE PROVIDER CATEGORY NAME
    // =========================================================

    private void updateProviderCategoryName(
            CategoryUpdatedEvent event
    ) {

        List<ProviderSearchDocument> providers =
                providerRepository.findByServiceCategoryId(
                        event.categoryId()
                );

        for (ProviderSearchDocument provider : providers) {

            List<ProviderSearchDocument.ServiceSearchItem> updatedServices =
                    getUpdatedServices(provider, event);

            ProviderSearchDocument updatedProvider =
                    copyProviderWithServices(
                            provider,
                            updatedServices
                    );

            providerRepository.save(updatedProvider);
        }
    }

    // =========================================================
    // UPDATE SERVICES WHEN CATEGORY CHANGES
    // =========================================================

    private List<ProviderSearchDocument.ServiceSearchItem> getUpdatedServices(
            ProviderSearchDocument provider,
            CategoryUpdatedEvent event
    ) {

        List<ProviderSearchDocument.ServiceSearchItem> updatedServices =
                new ArrayList<>();

        if (provider.getServices() == null) {
            return updatedServices;
        }

        for (ProviderSearchDocument.ServiceSearchItem service :
                provider.getServices()) {

            if (event.categoryId().equals(service.categoryId())) {

                updatedServices.add(
                        new ProviderSearchDocument.ServiceSearchItem(
                                service.providerServiceId(),
                                service.serviceItemId(),
                                service.serviceName(),
                                service.categoryId(),
                                event.name(),
                                service.price(),
                                service.active()
                        )
                );

            } else {
                updatedServices.add(service);
            }
        }

        return updatedServices;
    }

    // =========================================================
    // UPDATE PROVIDER SERVICE ITEM
    // =========================================================

    private void updateProviderServiceItem(
            ServiceItemUpdatedEvent event,
            String categoryName
    ) {

        List<ProviderSearchDocument> providers =
                providerRepository.findByServiceItemId(
                        event.serviceItemId()
                );

        for (ProviderSearchDocument provider : providers) {

            List<ProviderSearchDocument.ServiceSearchItem> updatedServices =
                    new ArrayList<>();

            if (provider.getServices() == null) {
                continue;
            }

            for (ProviderSearchDocument.ServiceSearchItem service :
                    provider.getServices()) {

                if (event.serviceItemId().equals(service.serviceItemId())) {

                    ProviderSearchDocument.ServiceSearchItem updatedService =
                            new ProviderSearchDocument.ServiceSearchItem(
                                    service.providerServiceId(),
                                    service.serviceItemId(),
                                    event.name(),
                                    event.categoryId(),
                                    categoryName,
                                    service.price(),
                                    event.active()
                            );

                    updatedServices.add(updatedService);

                } else {

                    updatedServices.add(service);
                }
            }

            ProviderSearchDocument updatedProvider =
                    copyProviderWithServices(
                            provider,
                            updatedServices
                    );

            providerRepository.save(updatedProvider);
        }
    }

    // =========================================================
    // COPY PROVIDER WITH UPDATED SERVICES
    // =========================================================


    private ProviderSearchDocument copyProviderWithServices(
            ProviderSearchDocument provider,
            List<ProviderSearchDocument.ServiceSearchItem> services
    ) {

        return new ProviderSearchDocument(
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
    }

}