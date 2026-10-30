package com.pm.searchservice.infrastructure.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.catalog.CategoryCreatedEvent;
import com.pm.contracts.catalog.CategoryDeletedEvent;
import com.pm.contracts.catalog.CategoryUpdatedEvent;
import com.pm.contracts.catalog.ServiceItemCreatedEvent;
import com.pm.contracts.catalog.ServiceItemDeletedEvent;
import com.pm.contracts.catalog.ServiceItemUpdatedEvent;
import com.pm.contracts.common.DomainEvent;
import com.pm.contracts.provider.ProviderCreatedEvent;
import com.pm.contracts.provider.ProviderDeletedEvent;
import com.pm.contracts.provider.ProviderServiceCreatedEvent;
import com.pm.contracts.provider.ProviderServiceDeletedEvent;
import com.pm.contracts.provider.ProviderServiceUpdatedEvent;
import com.pm.contracts.provider.ProviderUpdatedEvent;
import com.pm.contracts.rating.CustomerRatedEvent;
import com.pm.contracts.rating.CustomerRatingDeletedEvent;
import com.pm.contracts.rating.CustomerRatingUpdatedEvent;
import com.pm.searchservice.infrastructure.consumer.catalog.CatalogEventHandler;
import com.pm.searchservice.infrastructure.consumer.catalog.CatalogEventTypeResolver;
import com.pm.searchservice.infrastructure.consumer.provider.ProviderEventHandler;
import com.pm.searchservice.infrastructure.consumer.provider.ProviderEventTypeResolver;
import com.pm.searchservice.infrastructure.consumer.providerservice.ProviderServiceEventHandler;
import com.pm.searchservice.infrastructure.consumer.providerservice.ProviderServiceEventTypeResolver;
import com.pm.searchservice.infrastructure.consumer.rating.RatingEventHandler;
import com.pm.searchservice.infrastructure.consumer.rating.RatingEventTypeResolver;
import com.pm.searchservice.infrastructure.idempotency.EventInboxService;
import com.pm.searchservice.infrastructure.idempotency.InboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SearchEventProcessor {

    private static final int SUPPORTED_EVENT_VERSION = 1;

    private final ObjectMapper objectMapper;

    private final ProviderEventTypeResolver providerResolver;
    private final ProviderEventHandler providerHandler;

    private final ProviderServiceEventTypeResolver providerServiceResolver;
    private final ProviderServiceEventHandler providerServiceHandler;

    private final CatalogEventTypeResolver catalogResolver;
    private final CatalogEventHandler catalogHandler;

    private final RatingEventTypeResolver ratingResolver;
    private final RatingEventHandler ratingHandler;

    private final EventInboxService inboxService;

    private final String providerTopic;
    private final String providerServiceTopic;
    private final String catalogCategoryTopic;
    private final String catalogServiceItemTopic;
    private final String ratingTopic;
    private static final Logger log =
            LoggerFactory.getLogger(SearchEventProcessor.class);

    public SearchEventProcessor(
            ObjectMapper objectMapper,
            ProviderEventTypeResolver providerResolver,
            ProviderEventHandler providerHandler,
            ProviderServiceEventTypeResolver providerServiceResolver,
            ProviderServiceEventHandler providerServiceHandler,
            CatalogEventTypeResolver catalogResolver,
            CatalogEventHandler catalogHandler,
            RatingEventTypeResolver ratingResolver,
            RatingEventHandler ratingHandler,
            EventInboxService inboxService,
            @Value("${search.kafka.provider-events-topic}")
            String providerTopic,
            @Value("${search.kafka.provider-service-events-topic}")
            String providerServiceTopic,
            @Value("${search.kafka.catalog-category-events-topic}")
            String catalogCategoryTopic,
            @Value("${search.kafka.catalog-service-item-events-topic}")
            String catalogServiceItemTopic,
            @Value("${search.kafka.rating-events-topic}")
            String ratingTopic
    ) {
        this.objectMapper = objectMapper;
        this.providerResolver = providerResolver;
        this.providerHandler = providerHandler;
        this.providerServiceResolver = providerServiceResolver;
        this.providerServiceHandler = providerServiceHandler;
        this.catalogResolver = catalogResolver;
        this.catalogHandler = catalogHandler;
        this.ratingResolver = ratingResolver;
        this.ratingHandler = ratingHandler;
        this.inboxService = inboxService;
        this.providerTopic = providerTopic;
        this.providerServiceTopic = providerServiceTopic;
        this.catalogCategoryTopic = catalogCategoryTopic;
        this.catalogServiceItemTopic = catalogServiceItemTopic;
        this.ratingTopic = ratingTopic;
    }

    public void processFromKafka(
            String topic,
            String message
    ) {
        ParsedEvent parsed;

        try {
            parsed = parse(topic, message);
        } catch (RuntimeException ex) {
            log.error(
                    "Failed to parse catalog/search event. topic={}",
                    topic,
                    ex
            );
            throw ex;
        }

        if (parsed == null) {
            log.warn(
                    "Event ignored because its type is unsupported. topic={}",
                    topic
            );
            return;
        }

        log.info(
                "Event parsed successfully. topic={}, eventType={}, " +
                        "eventId={}, aggregateId={}, version={}",
                topic,
                parsed.eventType(),
                parsed.eventId(),
                parsed.aggregateId(),
                parsed.eventVersion()
        );

        Optional<UUID> processingToken =
                inboxService.accept(
                        parsed.eventId(),
                        topic,
                        parsed.eventType(),
                        parsed.eventVersion(),
                        parsed.aggregateId(),
                        parsed.payload()
                );

        if (processingToken.isEmpty()) {
            log.warn(
                    "Event was not claimed for processing. " +
                            "topic={}, eventType={}, eventId={}, aggregateId={}",
                    topic,
                    parsed.eventType(),
                    parsed.eventId(),
                    parsed.aggregateId()
            );
            return;
        }

        UUID token = processingToken.get();

        try {
            log.info(
                    "Dispatching event to handler. topic={}, eventType={}, " +
                            "eventId={}, aggregateId={}",
                    topic,
                    parsed.eventType(),
                    parsed.eventId(),
                    parsed.aggregateId()
            );

            dispatch(topic, parsed.event());

            inboxService.markProcessed(
                    parsed.eventId(),
                    token
            );

            log.info(
                    "Event processed successfully. topic={}, eventType={}, " +
                            "eventId={}, aggregateId={}",
                    topic,
                    parsed.eventType(),
                    parsed.eventId(),
                    parsed.aggregateId()
            );

        } catch (RuntimeException ex) {
            log.error(
                    "Event processing failed. topic={}, eventType={}, " +
                            "eventId={}, aggregateId={}",
                    topic,
                    parsed.eventType(),
                    parsed.eventId(),
                    parsed.aggregateId(),
                    ex
            );

            inboxService.markPending(
                    parsed.eventId(),
                    token
            );

            throw ex;
        }
    }

    public void processFromInbox(
            InboxEvent inboxEvent
    ) {

        DomainEvent event =
                deserializeStoredEvent(inboxEvent);

        dispatch(
                inboxEvent.getTopic(),
                event
        );
    }

    private ParsedEvent parse(
            String topic,
            String message
    ) {

        try {

            JsonNode root = objectMapper.readTree(message);

            JsonNode typeNode = root.get("type");
            JsonNode versionNode = root.get("version");
            JsonNode payloadNode = root.get("payload");

            validateEnvelope(
                    typeNode,
                    versionNode,
                    payloadNode
            );

            String eventType = typeNode.asText();
            int envelopeVersion = versionNode.asInt();

            Class<? extends DomainEvent> eventClass =
                    resolveEventClass(
                            topic,
                            eventType
                    );

            /*
             * Unknown event types are intentionally ignored.
             */
            if (eventClass == null) {
                log.warn(
                        "Ignoring unsupported event type. topic={}, eventType={}",
                        topic,
                        typeNode.asText()
                );

                return null;
            }

            validateEventVersion(
                    eventType,
                    envelopeVersion
            );

            DomainEvent event =
                    objectMapper.treeToValue(
                            payloadNode,
                            eventClass
                    );

            validateDeserializedEvent(
                    eventType,
                    envelopeVersion,
                    event
            );

            /*
             * The canonical event ID and aggregate ID come from
             * the concrete DomainEvent payload, not the envelope root.
             */
            UUID eventId = event.eventId();
            UUID aggregateId = event.aggregateId();

            if (eventId == null) {
                throw new IllegalArgumentException(
                        "Domain event ID is missing. type="
                                + eventType
                );
            }

            return new ParsedEvent(
                    eventId,
                    eventType,
                    envelopeVersion,
                    aggregateId,
                    payloadNode.toString(),
                    event
            );

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Failed to parse search event. topic="
                            + topic,
                    ex
            );
        }
    }

    private void validateDeserializedEvent(
            String envelopeType,
            int envelopeVersion,
            DomainEvent event
    ) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Deserialized domain event is null"
            );
        }

        if (event.eventId() == null) {
            throw new IllegalArgumentException(
                    "Domain event ID is missing. type="
                            + envelopeType
            );
        }

        if (!envelopeType.equals(event.eventType())) {
            throw new IllegalArgumentException(
                    "Envelope type does not match payload event type. "
                            + "envelopeType=" + envelopeType
                            + ", payloadType=" + event.eventType()
            );
        }

        if (envelopeVersion != event.eventVersion()) {
            throw new IllegalArgumentException(
                    "Envelope version does not match payload event version. "
                            + "type=" + envelopeType
                            + ", envelopeVersion=" + envelopeVersion
                            + ", payloadVersion=" + event.eventVersion()
            );
        }

        validateEventVersion(
                event.eventType(),
                event.eventVersion()
        );
    }

    private DomainEvent deserializeStoredEvent(
            InboxEvent inboxEvent
    ) {

        try {

            Class<? extends DomainEvent> eventClass =
                    resolveEventClass(
                            inboxEvent.getTopic(),
                            inboxEvent.getEventType()
                    );

            if (eventClass == null) {
                throw new IllegalArgumentException(
                        "Unsupported stored event type: "
                                + inboxEvent.getEventType()
                );
            }

            validateEventVersion(
                    inboxEvent.getEventType(),
                    inboxEvent.getEventVersion()
            );

            DomainEvent event =
                    objectMapper.readValue(
                            inboxEvent.getPayload(),
                            eventClass
                    );

            validateDeserializedEvent(
                    inboxEvent.getEventType(),
                    inboxEvent.getEventVersion(),
                    event
            );

            if (!inboxEvent.getEventId().equals(event.eventId())) {
                throw new IllegalArgumentException(
                        "Stored inbox event ID does not match payload event ID. "
                                + "inboxEventId=" + inboxEvent.getEventId()
                                + ", payloadEventId=" + event.eventId()
                );
            }

            return event;

        } catch (Exception ex) {

            throw new RuntimeException(
                    "Failed to deserialize inbox event: "
                            + inboxEvent.getEventId(),
                    ex
            );
        }
    }

    private Class<? extends DomainEvent> resolveEventClass(
            String topic,
            String eventType
    ) {

        if (providerTopic.equals(topic)) {

            if (!providerResolver.supports(eventType)) {
                return null;
            }

            return providerResolver.resolve(eventType);
        }

        if (providerServiceTopic.equals(topic)) {

            if (!providerServiceResolver.supports(eventType)) {
                return null;
            }

            return providerServiceResolver.resolve(eventType);
        }

        if (
                catalogCategoryTopic.equals(topic)
                        || catalogServiceItemTopic.equals(topic)
        ) {

            if (!catalogResolver.supports(eventType)) {
                return null;
            }

            return catalogResolver.resolve(eventType);
        }

        if (ratingTopic.equals(topic)) {

            if (!ratingResolver.supports(eventType)) {
                return null;
            }

            return ratingResolver.resolve(eventType);
        }

        throw new IllegalArgumentException(
                "Unknown Search Kafka topic: " + topic
        );
    }

    private void dispatch(
            String topic,
            DomainEvent event
    ) {

        if (providerTopic.equals(topic)) {
            dispatchProvider(event);
            return;
        }

        if (providerServiceTopic.equals(topic)) {
            dispatchProviderService(event);
            return;
        }

        if (
                catalogCategoryTopic.equals(topic)
                        || catalogServiceItemTopic.equals(topic)
        ) {
            dispatchCatalog(event);
            return;
        }

        if (ratingTopic.equals(topic)) {
            dispatchRating(event);
            return;
        }

        throw new IllegalArgumentException(
                "Unknown Search Kafka topic: " + topic
        );
    }

    private void dispatchProvider(
            DomainEvent event
    ) {

        switch (event) {

            case ProviderCreatedEvent created ->
                    providerHandler.handle(created);

            case ProviderUpdatedEvent updated ->
                    providerHandler.handle(updated);

            case ProviderDeletedEvent deleted ->
                    providerHandler.handle(deleted);

            default -> throw unsupported(event);
        }
    }

    private void dispatchProviderService(
            DomainEvent event
    ) {

        switch (event) {

            case ProviderServiceCreatedEvent created ->
                    providerServiceHandler.handle(created);

            case ProviderServiceUpdatedEvent updated ->
                    providerServiceHandler.handle(updated);

            case ProviderServiceDeletedEvent deleted ->
                    providerServiceHandler.handle(deleted);

            default -> throw unsupported(event);
        }
    }

    private void dispatchCatalog(
            DomainEvent event
    ) {

        switch (event) {

            case CategoryCreatedEvent created ->
                    catalogHandler.handle(created);

            case CategoryUpdatedEvent updated ->
                    catalogHandler.handle(updated);

            case CategoryDeletedEvent deleted ->
                    catalogHandler.handle(deleted);

            case ServiceItemCreatedEvent created ->
                    catalogHandler.handle(created);

            case ServiceItemUpdatedEvent updated ->
                    catalogHandler.handle(updated);

            case ServiceItemDeletedEvent deleted ->
                    catalogHandler.handle(deleted);

            default -> throw unsupported(event);
        }
    }

    private void dispatchRating(
            DomainEvent event
    ) {

        switch (event) {

            case CustomerRatedEvent created ->
                    ratingHandler.handle(created);

            case CustomerRatingUpdatedEvent updated ->
                    ratingHandler.handle(updated);

            case CustomerRatingDeletedEvent deleted ->
                    ratingHandler.handle(deleted);

            default -> throw unsupported(event);
        }
    }

    private RuntimeException unsupported(
            DomainEvent event
    ) {

        return new IllegalArgumentException(
                "Unsupported Search event: "
                        + event.getClass().getName()
        );
    }

    private void validateEnvelope(
            JsonNode typeNode,
            JsonNode versionNode,
            JsonNode payloadNode
    ) {

        if (
                typeNode == null
                        || typeNode.isNull()
                        || !typeNode.isTextual()
                        || typeNode.asText().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Event type is missing"
            );
        }

        if (
                versionNode == null
                        || versionNode.isNull()
                        || !versionNode.canConvertToInt()
        ) {
            throw new IllegalArgumentException(
                    "Event version is missing or invalid"
            );
        }

        if (
                payloadNode == null
                        || payloadNode.isNull()
                        || !payloadNode.isObject()
        ) {
            throw new IllegalArgumentException(
                    "Event payload is missing or invalid"
            );
        }
    }

    private void validateEventVersion(
            String eventType,
            int eventVersion
    ) {

        if (eventVersion != SUPPORTED_EVENT_VERSION) {
            throw new IllegalArgumentException(
                    "Unsupported event version. "
                            + "type=" + eventType
                            + ", version=" + eventVersion
                            + ", supportedVersion="
                            + SUPPORTED_EVENT_VERSION
            );
        }
    }

    private record ParsedEvent(
            UUID eventId,
            String eventType,
            int eventVersion,
            UUID aggregateId,
            String payload,
            DomainEvent event
    ) {
    }
}
