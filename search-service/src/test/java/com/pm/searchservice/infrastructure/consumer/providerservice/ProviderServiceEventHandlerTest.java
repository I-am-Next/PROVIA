package com.pm.searchservice.infrastructure.consumer.providerservice;

import com.pm.contracts.provider.ProviderServiceCreatedEvent;
import com.pm.contracts.provider.ProviderServiceDeletedEvent;
import com.pm.contracts.provider.ProviderServiceUpdatedEvent;
import com.pm.searchservice.domain.model.ProviderSearchDocument;
import com.pm.searchservice.domain.ports.out.ProviderSearchRepository;
import com.pm.searchservice.infrastructure.elasticsearch.CatalogServiceSearchDocumentEntity;
import com.pm.searchservice.infrastructure.elasticsearch.CatalogServiceSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderServiceEventHandlerTest {

    @Mock
    private ProviderSearchRepository providerRepository;

    @Mock
    private CatalogServiceSearchRepository catalogServiceRepository;

    private ProviderServiceEventHandler handler;

    private UUID providerId;
    private UUID providerServiceId;
    private UUID serviceItemId;
    private UUID categoryId;
    private UUID mediaId;

    private CatalogServiceSearchDocumentEntity catalogService;

    @BeforeEach
    void setUp() {
        handler = new ProviderServiceEventHandler(
                providerRepository,
                catalogServiceRepository
        );

        providerId = UUID.randomUUID();
        providerServiceId = UUID.randomUUID();
        serviceItemId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        mediaId = UUID.randomUUID();

        catalogService = new CatalogServiceSearchDocumentEntity(
                serviceItemId,
                "Electrical Installation",
                "Electrical installation and repair services",
                categoryId,
                "Electrical Services",
                true
        );
    }

    @Test
    void handleCreated_shouldAddServiceAndPreserveProviderData() {
        ProviderSearchDocument existing = createProvider(
                List.of(),
                mediaId
        );

        when(providerRepository.findByProviderId(providerId))
                .thenReturn(Optional.of(existing));

        when(catalogServiceRepository.findById(serviceItemId))
                .thenReturn(Optional.of(catalogService));

        ProviderServiceCreatedEvent event =
                new ProviderServiceCreatedEvent(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        providerServiceId,
                        providerId,
                        serviceItemId,
                        new BigDecimal("25000.00"),
                        true
                );

        handler.handle(event);

        ProviderSearchDocument saved = captureSavedProvider();

        assertEquals(providerId, saved.getProviderId());
        assertEquals("Newton Electrical", saved.getProviderName());
        assertEquals("Abuja", saved.getLocation());
        assertEquals(new BigDecimal("4.50"), saved.getRating());
        assertEquals(12, saved.getReviewCount());
        assertEquals(mediaId, saved.getProfilePhotoMediaId());

        assertEquals(1, saved.getServices().size());

        ProviderSearchDocument.ServiceSearchItem service =
                saved.getServices().getFirst();

        assertEquals(providerServiceId, service.providerServiceId());
        assertEquals(serviceItemId, service.serviceItemId());
        assertEquals("Electrical Installation", service.serviceName());
        assertEquals(categoryId, service.categoryId());
        assertEquals("Electrical Services", service.categoryName());
        assertEquals(new BigDecimal("25000.00"), service.price());
        assertTrue(service.active());
    }

    @Test
    void handleCreated_shouldReplaceExistingServiceWithSameId() {
        ProviderSearchDocument.ServiceSearchItem oldService =
                serviceItem(
                        providerServiceId,
                        UUID.randomUUID(),
                        "Old Service Name",
                        new BigDecimal("10000.00"),
                        true
                );

        ProviderSearchDocument existing = createProvider(
                List.of(oldService),
                mediaId
        );

        when(providerRepository.findByProviderId(providerId))
                .thenReturn(Optional.of(existing));

        when(catalogServiceRepository.findById(serviceItemId))
                .thenReturn(Optional.of(catalogService));

        ProviderServiceCreatedEvent event =
                new ProviderServiceCreatedEvent(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        providerServiceId,
                        providerId,
                        serviceItemId,
                        new BigDecimal("25000.00"),
                        true
                );

        handler.handle(event);

        ProviderSearchDocument saved = captureSavedProvider();

        assertEquals(1, saved.getServices().size());
        assertEquals(
                new BigDecimal("25000.00"),
                saved.getServices().getFirst().price()
        );
        assertEquals(
                "Electrical Installation",
                saved.getServices().getFirst().serviceName()
        );
        assertEquals(mediaId, saved.getProfilePhotoMediaId());
    }

    @Test
    void handleUpdated_shouldUpdateServiceAndKeepInactiveServiceInProjection() {
        UUID anotherServiceId = UUID.randomUUID();

        ProviderSearchDocument.ServiceSearchItem existingService =
                serviceItem(
                        providerServiceId,
                        serviceItemId,
                        "Old Name",
                        new BigDecimal("10000.00"),
                        true
                );

        ProviderSearchDocument.ServiceSearchItem anotherService =
                serviceItem(
                        anotherServiceId,
                        UUID.randomUUID(),
                        "Another Service",
                        new BigDecimal("15000.00"),
                        true
                );

        ProviderSearchDocument existing = createProvider(
                List.of(existingService, anotherService),
                mediaId
        );

        when(providerRepository.findByProviderId(providerId))
                .thenReturn(Optional.of(existing));

        when(catalogServiceRepository.findById(serviceItemId))
                .thenReturn(Optional.of(catalogService));

        ProviderServiceUpdatedEvent event =
                new ProviderServiceUpdatedEvent(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        providerServiceId,
                        providerId,
                        serviceItemId,
                        new BigDecimal("30000.00"),
                        false
                );

        handler.handle(event);

        ProviderSearchDocument saved = captureSavedProvider();

        assertEquals(2, saved.getServices().size());
        assertEquals(mediaId, saved.getProfilePhotoMediaId());

        ProviderSearchDocument.ServiceSearchItem updatedService =
                saved.getServices()
                        .stream()
                        .filter(service ->
                                service.providerServiceId()
                                        .equals(providerServiceId)
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                new BigDecimal("30000.00"),
                updatedService.price()
        );
        assertFalse(updatedService.active());

        assertTrue(
                saved.getServices()
                        .stream()
                        .anyMatch(service ->
                                service.providerServiceId()
                                        .equals(anotherServiceId)
                        )
        );
    }

    @Test
    void handleDeleted_shouldRemoveOnlySpecifiedService() {
        UUID anotherServiceId = UUID.randomUUID();

        ProviderSearchDocument.ServiceSearchItem serviceToDelete =
                serviceItem(
                        providerServiceId,
                        serviceItemId,
                        "Electrical Installation",
                        new BigDecimal("25000.00"),
                        true
                );

        ProviderSearchDocument.ServiceSearchItem serviceToKeep =
                serviceItem(
                        anotherServiceId,
                        UUID.randomUUID(),
                        "Electrical Repairs",
                        new BigDecimal("15000.00"),
                        true
                );

        ProviderSearchDocument existing = createProvider(
                List.of(serviceToDelete, serviceToKeep),
                mediaId
        );

        when(providerRepository.findByProviderId(providerId))
                .thenReturn(Optional.of(existing));

        ProviderServiceDeletedEvent event =
                new ProviderServiceDeletedEvent(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        providerServiceId,
                        providerId,
                        serviceItemId
                );

        handler.handle(event);

        ProviderSearchDocument saved = captureSavedProvider();

        assertEquals(1, saved.getServices().size());
        assertEquals(
                anotherServiceId,
                saved.getServices().getFirst().providerServiceId()
        );
        assertEquals(mediaId, saved.getProfilePhotoMediaId());

        // Deletion must not remove the provider document itself.
        verify(providerRepository, never())
                .deleteByProviderId(providerId);
    }

    @Test
    void handleCreated_shouldFailWhenProviderDocumentDoesNotExist() {
        when(providerRepository.findByProviderId(providerId))
                .thenReturn(Optional.empty());

        ProviderServiceCreatedEvent event =
                new ProviderServiceCreatedEvent(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        providerServiceId,
                        providerId,
                        serviceItemId,
                        new BigDecimal("25000.00"),
                        true
                );

        assertThrows(
                IllegalStateException.class,
                () -> handler.handle(event)
        );

        verify(catalogServiceRepository, never())
                .findById(any(UUID.class));

        verify(providerRepository, never()).save(any());
    }

    @Test
    void handleCreated_shouldFailWhenCatalogProjectionDoesNotExist() {
        ProviderSearchDocument existing = createProvider(
                List.of(),
                mediaId
        );

        when(providerRepository.findByProviderId(providerId))
                .thenReturn(Optional.of(existing));

        when(catalogServiceRepository.findById(serviceItemId))
                .thenReturn(Optional.empty());

        ProviderServiceCreatedEvent event =
                new ProviderServiceCreatedEvent(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        providerServiceId,
                        providerId,
                        serviceItemId,
                        new BigDecimal("25000.00"),
                        true
                );

        assertThrows(
                IllegalStateException.class,
                () -> handler.handle(event)
        );

        verify(providerRepository, never()).save(any());
    }

    private ProviderSearchDocument createProvider(
            List<ProviderSearchDocument.ServiceSearchItem> services,
            UUID profilePhotoMediaId
    ) {
        return new ProviderSearchDocument(
                providerId,
                "Newton Electrical",
                "Electrical installation and repairs",
                "Abuja",
                new BigDecimal("4.50"),
                12,
                true,
                profilePhotoMediaId,
                services
        );
    }

    private ProviderSearchDocument.ServiceSearchItem serviceItem(
            UUID providerServiceId,
            UUID serviceItemId,
            String serviceName,
            BigDecimal price,
            boolean active
    ) {
        return new ProviderSearchDocument.ServiceSearchItem(
                providerServiceId,
                serviceItemId,
                serviceName,
                categoryId,
                "Electrical Services",
                price,
                active
        );
    }

    private ProviderSearchDocument captureSavedProvider() {
        ArgumentCaptor<ProviderSearchDocument> captor =
                ArgumentCaptor.forClass(ProviderSearchDocument.class);

        verify(providerRepository).save(captor.capture());

        return captor.getValue();
    }
}
