package com.pm.searchservice.infrastructure.consumer.provider;

import com.pm.contracts.provider.ProviderCreatedEvent;
import com.pm.contracts.provider.ProviderDeletedEvent;
import com.pm.contracts.provider.ProviderUpdatedEvent;
import com.pm.searchservice.domain.model.ProviderSearchDocument;
import com.pm.searchservice.domain.ports.out.ProviderSearchRepository;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderEventHandlerTest {

    @Mock
    private ProviderSearchRepository providerRepository;

    private ProviderEventHandler handler;

    private UUID providerId;
    private UUID mediaId;

    @BeforeEach
    void setUp() {
        handler = new ProviderEventHandler(providerRepository);

        providerId = UUID.randomUUID();
        mediaId = UUID.randomUUID();
    }

    @Test
    void handleCreated_shouldSaveProviderWithProfilePhotoMediaId() {
        ProviderCreatedEvent event = new ProviderCreatedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                providerId,
                "auth-user-123",
                "Newton Electrical",
                "newton@example.com",
                "ELECTRICAL",
                "Electrical installation and repairs",
                "08012345678",
                true,
                mediaId
        );

        handler.handle(event);

        ArgumentCaptor<ProviderSearchDocument> captor =
                ArgumentCaptor.forClass(ProviderSearchDocument.class);

        verify(providerRepository).save(captor.capture());

        ProviderSearchDocument saved = captor.getValue();

        assertEquals(providerId, saved.getProviderId());
        assertEquals("Newton Electrical", saved.getProviderName());
        assertEquals(
                "Electrical installation and repairs",
                saved.getDescription()
        );
        assertTrue(saved.isVerified());
        assertEquals(mediaId, saved.getProfilePhotoMediaId());
        assertTrue(saved.getServices().isEmpty());
    }

    @Test
    void handleCreated_shouldAllowProviderWithoutProfilePhoto() {
        ProviderCreatedEvent event = new ProviderCreatedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                providerId,
                "auth-user-123",
                "Newton Electrical",
                "newton@example.com",
                "ELECTRICAL",
                "Electrical installation and repairs",
                "08012345678",
                true,
                null
        );

        handler.handle(event);

        ArgumentCaptor<ProviderSearchDocument> captor =
                ArgumentCaptor.forClass(ProviderSearchDocument.class);

        verify(providerRepository).save(captor.capture());

        assertNull(captor.getValue().getProfilePhotoMediaId());
    }

    @Test
    void handleUpdated_shouldReplacePhotoAndPreserveOtherProjectionFields() {
        UUID oldMediaId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();

        ProviderSearchDocument.ServiceSearchItem service =
                new ProviderSearchDocument.ServiceSearchItem(
                        serviceId,
                        UUID.randomUUID(),
                        "Electrical installation",
                        UUID.randomUUID(),
                        "Electrical Services",
                        new BigDecimal("25000.00"),
                        true
                );

        ProviderSearchDocument existing =
                new ProviderSearchDocument(
                        providerId,
                        "Old Business Name",
                        "Old description",
                        "Abuja",
                        new BigDecimal("4.50"),
                        12,
                        false,
                        oldMediaId,
                        List.of(service)
                );

        when(providerRepository.findByProviderId(providerId))
                .thenReturn(Optional.of(existing));

        ProviderUpdatedEvent event = new ProviderUpdatedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                providerId,
                "auth-user-123",
                "Newton Electrical",
                "newton@example.com",
                "ELECTRICAL",
                "Updated electrical services",
                "08012345678",
                true,
                mediaId
        );

        handler.handle(event);

        ArgumentCaptor<ProviderSearchDocument> captor =
                ArgumentCaptor.forClass(ProviderSearchDocument.class);

        verify(providerRepository).save(captor.capture());

        ProviderSearchDocument saved = captor.getValue();

        assertEquals(providerId, saved.getProviderId());
        assertEquals("Newton Electrical", saved.getProviderName());
        assertEquals(
                "Updated electrical services",
                saved.getDescription()
        );
        assertEquals("Abuja", saved.getLocation());
        assertEquals(new BigDecimal("4.50"), saved.getRating());
        assertEquals(12, saved.getReviewCount());
        assertTrue(saved.isVerified());

        // The new photo reference replaces the old one.
        assertEquals(mediaId, saved.getProfilePhotoMediaId());

        // Data owned by other projections is preserved.
        assertEquals(List.of(service), saved.getServices());
    }

    @Test
    void handleUpdated_shouldClearPhotoWhenMediaIdIsNull() {
        ProviderSearchDocument existing =
                new ProviderSearchDocument(
                        providerId,
                        "Newton Electrical",
                        "Electrical services",
                        "Abuja",
                        new BigDecimal("4.50"),
                        12,
                        true,
                        UUID.randomUUID(),
                        List.of()
                );

        when(providerRepository.findByProviderId(providerId))
                .thenReturn(Optional.of(existing));

        ProviderUpdatedEvent event = new ProviderUpdatedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                providerId,
                "auth-user-123",
                "Newton Electrical",
                "newton@example.com",
                "ELECTRICAL",
                "Electrical services",
                "08012345678",
                true,
                null
        );

        handler.handle(event);

        ArgumentCaptor<ProviderSearchDocument> captor =
                ArgumentCaptor.forClass(ProviderSearchDocument.class);

        verify(providerRepository).save(captor.capture());

        assertNull(captor.getValue().getProfilePhotoMediaId());
    }

    @Test
    void handleUpdated_shouldCreateDocumentWhenExistingDocumentIsMissing() {
        when(providerRepository.findByProviderId(providerId))
                .thenReturn(Optional.empty());

        ProviderUpdatedEvent event = new ProviderUpdatedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                providerId,
                "auth-user-123",
                "Newton Electrical",
                "newton@example.com",
                "ELECTRICAL",
                "Electrical services",
                "08012345678",
                true,
                mediaId
        );

        handler.handle(event);

        ArgumentCaptor<ProviderSearchDocument> captor =
                ArgumentCaptor.forClass(ProviderSearchDocument.class);

        verify(providerRepository).save(captor.capture());

        ProviderSearchDocument saved = captor.getValue();

        assertEquals(providerId, saved.getProviderId());
        assertEquals("Newton Electrical", saved.getProviderName());
        assertTrue(saved.isVerified());
        assertEquals(mediaId, saved.getProfilePhotoMediaId());
        assertTrue(saved.getServices().isEmpty());
        assertNull(saved.getLocation());
        assertNull(saved.getRating());
        assertEquals(0, saved.getReviewCount());
    }

    @Test
    void handleDeleted_shouldDeleteProviderById() {
        ProviderDeletedEvent event = new ProviderDeletedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                providerId
        );

        handler.handle(event);

        verify(providerRepository).deleteByProviderId(providerId);
        verify(providerRepository, never()).save(any());
    }
}
