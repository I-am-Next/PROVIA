package com.pm.servicecatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.contracts.catalog.CategoryCreatedEvent;
import com.pm.servicecatalog.infrastructure.kafka.CatalogEventProducer;
import com.pm.servicecatalog.infrastructure.outbox.OutboxDomainEventPublisher;
import com.pm.servicecatalog.infrastructure.outbox.OutboxEvent;
import com.pm.servicecatalog.infrastructure.outbox.OutboxEventMapper;
import com.pm.servicecatalog.infrastructure.outbox.OutboxEventPublisher;
import com.pm.servicecatalog.infrastructure.outbox.OutboxEventRepository;
import com.pm.servicecatalog.infrastructure.outbox.OutboxEventTransactionService;
import com.pm.servicecatalog.infrastructure.outbox.OutboxEventType;
import com.pm.servicecatalog.infrastructure.outbox.OutboxEventTypeResolver;
import com.pm.servicecatalog.infrastructure.outbox.OutboxStatus;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OutboxTest {

    @Mock
    private OutboxEventRepository repository;

    @Mock
    private CatalogEventProducer producer;

    @Mock
    private OutboxEventTransactionService transactionService;

    private OutboxEventMapper mapper;

    private OutboxDomainEventPublisher domainEventPublisher;

    private OutboxEventPublisher outboxEventPublisher;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        mapper = new OutboxEventMapper(
                new ObjectMapper().findAndRegisterModules(),
                new OutboxEventTypeResolver()
        );

        domainEventPublisher = new OutboxDomainEventPublisher(
                repository,
                mapper
        );

        outboxEventPublisher = new OutboxEventPublisher(
                transactionService,
                producer
        );
    }

    @Test
    void mapperShouldConvertDomainEventToOutboxEvent() {
        UUID categoryId = UUID.randomUUID();

        CategoryCreatedEvent event = new CategoryCreatedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                categoryId,
                "Plumbing",
                "Plumbing services",
                true
        );

        OutboxEvent outboxEvent = mapper.toOutboxEvent(event);

        assertEquals("CATEGORY", outboxEvent.getAggregateType());
        assertEquals(categoryId.toString(), outboxEvent.getAggregateId());
        assertEquals(
                OutboxEventType.CATEGORY_CREATED.name(),
                outboxEvent.getEventType()
        );
        assertNotNull(outboxEvent.getPayload());
        assertFalse(outboxEvent.getPayload().isBlank());
        assertEquals(OutboxStatus.PENDING, outboxEvent.getStatus());
        assertEquals(0, outboxEvent.getRetryCount());
    }

    @Test
    void domainEventPublisherShouldPersistMappedOutboxEvent() {
        UUID categoryId = UUID.randomUUID();

        CategoryCreatedEvent event = new CategoryCreatedEvent(
                UUID.randomUUID(),
                1,
                Instant.now(),
                categoryId,
                "Plumbing",
                "Plumbing services",
                true
        );

        domainEventPublisher.publish(event);

        ArgumentCaptor<OutboxEvent> captor =
                ArgumentCaptor.forClass(OutboxEvent.class);

        verify(repository).save(captor.capture());

        OutboxEvent saved = captor.getValue();

        assertEquals("CATEGORY", saved.getAggregateType());
        assertEquals(categoryId.toString(), saved.getAggregateId());
        assertEquals(
                OutboxEventType.CATEGORY_CREATED.name(),
                saved.getEventType()
        );
        assertEquals(OutboxStatus.PENDING, saved.getStatus());
    }

    @Test
    void outboxEventShouldScheduleRetryAfterFailure() {
        OutboxEvent event = new OutboxEvent(
                "CATEGORY",
                UUID.randomUUID().toString(),
                "CATEGORY_CREATED",
                "{}"
        );

        assertEquals(OutboxStatus.PENDING, event.getStatus());

        event.markProcessing();

        assertEquals(OutboxStatus.PROCESSING, event.getStatus());

        event.scheduleRetry();

        assertEquals(OutboxStatus.PENDING, event.getStatus());
        assertEquals(1, event.getRetryCount());
        assertNotNull(event.getNextRetryAt());
    }

    @Test
    void publisherShouldMarkEventProcessedAfterSuccessfulKafkaSend()
            throws Exception {

        OutboxEvent event = new OutboxEvent(
                "CATEGORY",
                UUID.randomUUID().toString(),
                "CATEGORY_CREATED",
                "{}"
        );

        event.markProcessing();

        when(transactionService.claimReadyEvents())
                .thenReturn(List.of(event));

        @SuppressWarnings("unchecked")
        SendResult<String, byte[]> sendResult = mock(SendResult.class);

        RecordMetadata metadata = mock(RecordMetadata.class);

        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(metadata.partition()).thenReturn(0);
        when(metadata.offset()).thenReturn(10L);

        when(producer.publishCategoryEvent(
                anyString(),
                anyString(),
                any(byte[].class)
        )).thenReturn(
                CompletableFuture.completedFuture(sendResult)
        );

        outboxEventPublisher.publishEvents();

        verify(transactionService).claimReadyEvents();

        verify(producer).publishCategoryEvent(
                eq("CATEGORY_CREATED"),
                eq(event.getAggregateId()),
                any(byte[].class)
        );

        verify(transactionService).markProcessed(event.getId());

        verify(transactionService, never())
                .scheduleRetry(any(UUID.class));
    }

    @Test
    void publisherShouldScheduleRetryWhenKafkaPublicationFails() {
        OutboxEvent event = new OutboxEvent(
                "CATEGORY",
                UUID.randomUUID().toString(),
                "CATEGORY_CREATED",
                "{}"
        );

        event.markProcessing();

        when(transactionService.claimReadyEvents())
                .thenReturn(List.of(event));

        CompletableFuture<SendResult<String, byte[]>> failedFuture =
                new CompletableFuture<>();

        failedFuture.completeExceptionally(
                new RuntimeException("Kafka unavailable")
        );

        when(producer.publishCategoryEvent(
                anyString(),
                anyString(),
                any(byte[].class)
        )).thenReturn(failedFuture);

        outboxEventPublisher.publishEvents();

        verify(transactionService).claimReadyEvents();

        verify(transactionService).scheduleRetry(event.getId());

        verify(transactionService, never())
                .markProcessed(any(UUID.class));
    }

    @Test
    void publisherShouldRouteServiceItemEventsToServiceItemProducer()
            throws Exception {

        OutboxEvent event = new OutboxEvent(
                "SERVICE_ITEM",
                UUID.randomUUID().toString(),
                "SERVICE_ITEM_CREATED",
                "{}"
        );

        event.markProcessing();

        when(transactionService.claimReadyEvents())
                .thenReturn(List.of(event));

        @SuppressWarnings("unchecked")
        SendResult<String, byte[]> sendResult = mock(SendResult.class);

        RecordMetadata metadata = mock(RecordMetadata.class);

        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(metadata.partition()).thenReturn(1);
        when(metadata.offset()).thenReturn(20L);

        when(producer.publishServiceItemEvent(
                anyString(),
                anyString(),
                any(byte[].class)
        )).thenReturn(
                CompletableFuture.completedFuture(sendResult)
        );

        outboxEventPublisher.publishEvents();

        verify(producer).publishServiceItemEvent(
                eq("SERVICE_ITEM_CREATED"),
                eq(event.getAggregateId()),
                any(byte[].class)
        );

        verify(producer, never()).publishCategoryEvent(
                anyString(),
                anyString(),
                any(byte[].class)
        );

        verify(transactionService).markProcessed(event.getId());
    }

    @Test
    void publisherShouldNotPublishWhenNoEventsAreReady() {
        when(transactionService.claimReadyEvents())
                .thenReturn(List.of());

        outboxEventPublisher.publishEvents();

        verifyNoInteractions(producer);
        verify(transactionService).claimReadyEvents();
        verify(transactionService, never())
                .markProcessed(any(UUID.class));
        verify(transactionService, never())
                .scheduleRetry(any(UUID.class));
    }
}
