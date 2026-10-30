package com.pm.searchservice.infrastructure.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.provider.ProviderCreatedEvent;
import com.pm.searchservice.infrastructure.consumer.catalog.CatalogEventHandler;
import com.pm.searchservice.infrastructure.consumer.catalog.CatalogEventTypeResolver;
import com.pm.searchservice.infrastructure.consumer.provider.ProviderEventHandler;
import com.pm.searchservice.infrastructure.consumer.provider.ProviderEventTypeResolver;
import com.pm.searchservice.infrastructure.consumer.providerservice.ProviderServiceEventHandler;
import com.pm.searchservice.infrastructure.consumer.providerservice.ProviderServiceEventTypeResolver;
import com.pm.searchservice.infrastructure.consumer.rating.RatingEventHandler;
import com.pm.searchservice.infrastructure.consumer.rating.RatingEventTypeResolver;
import com.pm.searchservice.infrastructure.idempotency.EventInboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchEventProcessorTest {

    private static final String PROVIDER_TOPIC = "provider-events";
    private static final String PROVIDER_SERVICE_TOPIC = "provider-service-events";
    private static final String CATEGORY_TOPIC = "category-events";
    private static final String SERVICE_ITEM_TOPIC = "service-item-events";
    private static final String RATING_TOPIC = "rating-events";

    private static final UUID EVENT_ID =
            UUID.fromString("5578d1b9-51eb-40c0-bf94-5cdd46ab291f");

    private static final UUID PROVIDER_ID =
            UUID.fromString("5c456761-2728-4b32-a2b4-2141e6d270e5");

    private static final UUID PROCESSING_TOKEN =
            UUID.fromString("c34b4b62-6b55-46f2-937d-01f1e6e50f2c");

    @Mock
    private ProviderEventTypeResolver providerResolver;

    @Mock
    private ProviderEventHandler providerHandler;

    @Mock
    private ProviderServiceEventTypeResolver providerServiceResolver;

    @Mock
    private ProviderServiceEventHandler providerServiceHandler;

    @Mock
    private CatalogEventTypeResolver catalogResolver;

    @Mock
    private CatalogEventHandler catalogHandler;

    @Mock
    private RatingEventTypeResolver ratingResolver;

    @Mock
    private RatingEventHandler ratingHandler;

    @Mock
    private EventInboxService inboxService;

    private SearchEventProcessor processor;

    private final ObjectMapper objectMapper =
            new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        processor = new SearchEventProcessor(
                objectMapper,
                providerResolver,
                providerHandler,
                providerServiceResolver,
                providerServiceHandler,
                catalogResolver,
                catalogHandler,
                ratingResolver,
                ratingHandler,
                inboxService,
                PROVIDER_TOPIC,
                PROVIDER_SERVICE_TOPIC,
                CATEGORY_TOPIC,
                SERVICE_ITEM_TOPIC,
                RATING_TOPIC
        );
    }

    @Test
    void shouldUseCanonicalEventIdAndAggregateIdFromPayload() {
        stubProviderCreatedEvent();

        when(inboxService.accept(
                eq(EVENT_ID),
                eq(PROVIDER_TOPIC),
                eq("PROVIDER_CREATED"),
                eq(1),
                eq(PROVIDER_ID),
                anyString()
        )).thenReturn(Optional.of(PROCESSING_TOKEN));

        processor.processFromKafka(
                PROVIDER_TOPIC,
                providerCreatedMessage()
        );

        verify(inboxService).accept(
                eq(EVENT_ID),
                eq(PROVIDER_TOPIC),
                eq("PROVIDER_CREATED"),
                eq(1),
                eq(PROVIDER_ID),
                anyString()
        );

        verify(providerHandler).handle(
                any(ProviderCreatedEvent.class)
        );

        verify(inboxService).markProcessed(
                EVENT_ID,
                PROCESSING_TOKEN
        );

        verify(inboxService, never()).markPending(
                any(UUID.class),
                any(UUID.class)
        );
    }

    @Test
    void shouldNotDispatchEventWhenInboxDeclinesClaim() {
        stubProviderCreatedEvent();

        when(inboxService.accept(
                eq(EVENT_ID),
                eq(PROVIDER_TOPIC),
                eq("PROVIDER_CREATED"),
                eq(1),
                eq(PROVIDER_ID),
                anyString()
        )).thenReturn(Optional.empty());

        processor.processFromKafka(
                PROVIDER_TOPIC,
                providerCreatedMessage()
        );

        verify(providerHandler, never())
                .handle(any(ProviderCreatedEvent.class));

        verify(inboxService, never())
                .markProcessed(any(UUID.class), any(UUID.class));

        verify(inboxService, never())
                .markPending(any(UUID.class), any(UUID.class));
    }

    @Test
    void shouldRejectPayloadWhenEventIdIsMissing() {
        stubProviderCreatedEvent();

        String message = """
                {
                  "type": "PROVIDER_CREATED",
                  "version": 1,
                  "payload": {
                    "eventVersion": 1,
                    "occurredAt": "2026-08-13T21:37:43Z",
                    "providerId": "%s"
                  }
                }
                """.formatted(PROVIDER_ID);

        assertThrows(
                RuntimeException.class,
                () -> processor.processFromKafka(
                        PROVIDER_TOPIC,
                        message
                )
        );

        verifyNoInteractions(inboxService);
        verifyNoInteractions(providerHandler);
    }

    @Test
    void shouldRejectEnvelopeAndPayloadVersionMismatch() {
        stubProviderCreatedEvent();

        String message = providerCreatedMessage()
                .replace(
                        "\"eventVersion\": 1",
                        "\"eventVersion\": 2"
                );

        assertThrows(
                RuntimeException.class,
                () -> processor.processFromKafka(
                        PROVIDER_TOPIC,
                        message
                )
        );

        verifyNoInteractions(inboxService);
        verifyNoInteractions(providerHandler);
    }

    @Test
    void shouldIgnoreUnsupportedProviderEventType() {
        String message = providerCreatedMessage()
                .replace(
                        "PROVIDER_CREATED",
                        "PROVIDER_UNKNOWN"
                );

        when(providerResolver.supports("PROVIDER_UNKNOWN"))
                .thenReturn(false);

        processor.processFromKafka(
                PROVIDER_TOPIC,
                message
        );

        verifyNoInteractions(inboxService);
        verifyNoInteractions(providerHandler);
    }

    @Test
    void shouldUseSameEventIdWhenSameEventIsDeliveredAgain() {
        stubProviderCreatedEvent();

        when(inboxService.accept(
                eq(EVENT_ID),
                eq(PROVIDER_TOPIC),
                eq("PROVIDER_CREATED"),
                eq(1),
                eq(PROVIDER_ID),
                anyString()
        )).thenReturn(
                Optional.of(PROCESSING_TOKEN),
                Optional.empty()
        );

        String compactMessage = providerCreatedMessage();

        String reformattedMessage = """
                {
                  "type": "PROVIDER_CREATED",
                  "version": 1,
                  "payload": {
                    "eventId": "%s",
                    "eventVersion": 1,
                    "occurredAt": "2026-08-13T21:37:43Z",
                    "providerId": "%s",
                    "authUserId": "auth-user-1",
                    "businessName": "Test Plumbing",
                    "email": "provider@example.com",
                    "category": "Plumbing",
                    "description": "Plumbing services",
                    "phone": "08000000000",
                    "verified": false,
                    "profilePhotoMediaId": null
                  }
                }
                """.formatted(EVENT_ID, PROVIDER_ID);

        processor.processFromKafka(
                PROVIDER_TOPIC,
                compactMessage
        );

        processor.processFromKafka(
                PROVIDER_TOPIC,
                reformattedMessage
        );

        verify(inboxService, times(2)).accept(
                eq(EVENT_ID),
                eq(PROVIDER_TOPIC),
                eq("PROVIDER_CREATED"),
                eq(1),
                eq(PROVIDER_ID),
                anyString()
        );

        verify(providerHandler, times(1))
                .handle(any(ProviderCreatedEvent.class));

        verify(inboxService, times(1))
                .markProcessed(EVENT_ID, PROCESSING_TOKEN);
    }

    private void stubProviderCreatedEvent() {
        when(providerResolver.supports("PROVIDER_CREATED"))
                .thenReturn(true);

        doReturn(ProviderCreatedEvent.class)
                .when(providerResolver)
                .resolve("PROVIDER_CREATED");
    }

    private String providerCreatedMessage() {
        return """
                {
                  "type": "PROVIDER_CREATED",
                  "version": 1,
                  "payload": {
                    "eventId": "%s",
                    "eventVersion": 1,
                    "occurredAt": "2026-08-13T21:37:43Z",
                    "providerId": "%s",
                    "authUserId": "auth-user-1",
                    "businessName": "Test Plumbing",
                    "email": "provider@example.com",
                    "category": "Plumbing",
                    "description": "Plumbing services",
                    "phone": "08000000000",
                    "verified": false,
                    "profilePhotoMediaId": null
                  }
                }
                """.formatted(EVENT_ID, PROVIDER_ID);
    }
}
