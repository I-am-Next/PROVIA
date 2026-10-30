package com.pm.searchservice.infrastructure.consumer.rating;

import com.pm.contracts.rating.CustomerRatedEvent;
import com.pm.contracts.rating.CustomerRatingDeletedEvent;
import com.pm.contracts.rating.CustomerRatingUpdatedEvent;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingEventHandlerTest {

    @Mock
    private ProviderSearchRepository providerRepository;

    private RatingEventHandler handler;

    private UUID providerId;
    private UUID mediaId;
    private ProviderSearchDocument existingProvider;

    @BeforeEach
    void setUp() {
        handler = new RatingEventHandler(providerRepository);

        providerId = UUID.randomUUID();
        mediaId = UUID.randomUUID();

        ProviderSearchDocument.ServiceSearchItem service =
                new ProviderSearchDocument.ServiceSearchItem(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Electrical Installation",
                        UUID.randomUUID(),
                        "Electrical Services",
                        new BigDecimal("25000.00"),
                        true
                );

        existingProvider = new ProviderSearchDocument(
                providerId,
                "Newton Electrical",
                "Electrical installation and repairs",
                "Abuja",
                new BigDecimal("4.50"),
                12,
                true,
                mediaId,
                List.of(service)
        );
    }

    @Test
    void handleCustomerRated_shouldUpdateRatingAndPreserveProviderData() {
        when(providerRepository.findByProviderId(providerId))
                .thenReturn(Optional.of(existingProvider));

        CustomerRatedEvent event = new CustomerRatedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                UUID.randomUUID(), // bookingId
                UUID.randomUUID(), // ratingId
                providerId,
                UUID.randomUUID(), // customerId
                5,
                4.75,
                13L
        );

        handler.handle(event);

        ProviderSearchDocument saved = captureSavedProvider();

        assertEquals(new BigDecimal("4.75"), saved.getRating());
        assertEquals(13, saved.getReviewCount());
        assertProviderDataPreserved(saved);
    }

    @Test
    void handleCustomerRatingUpdated_shouldUpdateRatingAndPreserveProviderData() {
        when(providerRepository.findByProviderId(providerId))
                .thenReturn(Optional.of(existingProvider));

        CustomerRatingUpdatedEvent event =
                new CustomerRatingUpdatedEvent(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        UUID.randomUUID(), // bookingId
                        UUID.randomUUID(), // ratingId
                        providerId,
                        UUID.randomUUID(), // customerId
                        3,
                        4.25,
                        10L
                );

        handler.handle(event);

        ProviderSearchDocument saved = captureSavedProvider();

        assertEquals(new BigDecimal("4.25"), saved.getRating());
        assertEquals(10, saved.getReviewCount());
        assertProviderDataPreserved(saved);
    }

    @Test
    void handleCustomerRatingDeleted_shouldUpdateRatingSummaryAndPreserveProviderData() {
        when(providerRepository.findByProviderId(providerId))
                .thenReturn(Optional.of(existingProvider));

        CustomerRatingDeletedEvent event =
                new CustomerRatingDeletedEvent(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        UUID.randomUUID(), // bookingId
                        UUID.randomUUID(), // ratingId
                        providerId,
                        UUID.randomUUID(), // customerId
                        4,
                        4.25,
                        11L
                );

        handler.handle(event);

        ProviderSearchDocument saved = captureSavedProvider();

        assertEquals(new BigDecimal("4.25"), saved.getRating());
        assertEquals(11, saved.getReviewCount());
        assertProviderDataPreserved(saved);
    }

    @Test
    void handleRatingEvent_shouldAllowNullRatingSummaryValues() {
        when(providerRepository.findByProviderId(providerId))
                .thenReturn(Optional.of(existingProvider));

        CustomerRatedEvent event = new CustomerRatedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                providerId,
                UUID.randomUUID(),
                5,
                null,
                null
        );

        handler.handle(event);

        ProviderSearchDocument saved = captureSavedProvider();

        assertNull(saved.getRating());
        assertEquals(0, saved.getReviewCount());
        assertProviderDataPreserved(saved);
    }

    @Test
    void handleRatingEvent_shouldFailWhenProviderDocumentDoesNotExist() {
        when(providerRepository.findByProviderId(providerId))
                .thenReturn(Optional.empty());

        CustomerRatedEvent event = new CustomerRatedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                providerId,
                UUID.randomUUID(),
                5,
                4.75,
                13L
        );

        assertThrows(
                IllegalStateException.class,
                () -> handler.handle(event)
        );
    }

    private ProviderSearchDocument captureSavedProvider() {
        ArgumentCaptor<ProviderSearchDocument> captor =
                ArgumentCaptor.forClass(ProviderSearchDocument.class);

        verify(providerRepository).save(captor.capture());

        return captor.getValue();
    }

    private void assertProviderDataPreserved(
            ProviderSearchDocument saved
    ) {
        assertEquals(providerId, saved.getProviderId());
        assertEquals("Newton Electrical", saved.getProviderName());
        assertEquals(
                "Electrical installation and repairs",
                saved.getDescription()
        );
        assertEquals("Abuja", saved.getLocation());
        assertEquals(mediaId, saved.getProfilePhotoMediaId());
        assertEquals(
                existingProvider.getServices(),
                saved.getServices()
        );
        assertTrue(saved.isVerified());
    }
}
