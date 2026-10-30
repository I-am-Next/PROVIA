package com.pm.providerservice.application;

import com.pm.providerservice.api.dto.request.CreateProviderServiceRequest;
import com.pm.providerservice.api.dto.request.UpdateProviderServiceRequest;
import com.pm.providerservice.api.dto.response.ProviderServiceDetailsResponse;
import com.pm.providerservice.api.dto.response.ProviderServiceResponse;
import com.pm.providerservice.application.mapper.ProviderServiceEventMapper;
import com.pm.providerservice.application.ports.OutboxDomainEventPublisher;
import com.pm.providerservice.application.service.impl.ProviderServiceServiceImpl;
import com.pm.providerservice.client.CatalogClient;
import com.pm.providerservice.client.dto.ApiResponse;
import com.pm.providerservice.client.dto.CatalogServiceItemResponse;
import com.pm.providerservice.domain.ProviderProfile;
import com.pm.providerservice.domain.ProviderService;
import com.pm.providerservice.exception.DuplicateProviderServiceException;
import com.pm.providerservice.exception.ProviderServiceOwnershipException;
import com.pm.providerservice.repository.ProviderProfileRepository;
import com.pm.providerservice.repository.ProviderServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.oauth2.jwt.Jwt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderServiceServiceImplTest {

    @Mock
    private ProviderServiceRepository repository;

    @Mock
    private ProviderProfileRepository profileRepository;

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private ProviderServiceEventMapper eventMapper;

    @Mock
    private OutboxDomainEventPublisher outboxPublisher;

    @InjectMocks
    private ProviderServiceServiceImpl service;

    private UUID authUserId;
    private UUID providerProfileId;
    private UUID serviceItemId;

    private Jwt jwt;

    @BeforeEach
    void setUp() {

        authUserId = UUID.randomUUID();
        providerProfileId = UUID.randomUUID();
        serviceItemId = UUID.randomUUID();

        jwt = Jwt.withTokenValue("test-token")
                .subject(authUserId.toString())
                .claim("email", "provider@example.com")
                .header("alg", "none")
                .build();
    }

    private ProviderProfile providerProfile() {

        ProviderProfile profile =
                new ProviderProfile(
                        authUserId.toString(),
                        "Emmanuel Services",
                        "Plumbing",
                        "provider@example.com"
                );

        ReflectionTestUtils.setField(
                profile,
                "id",
                providerProfileId
        );

        return profile;
    }

    private CatalogServiceItemResponse catalogService() {

        return new CatalogServiceItemResponse(
                serviceItemId,
                "Pipe Repair",
                "Professional pipe repair",
                UUID.randomUUID(),
                "Plumbing",
                true
        );
    }

    private void mockCatalogLookup() {

        when(catalogClient.getServiceById(serviceItemId))
                .thenReturn(
                        new ApiResponse<>(
                                true,
                                catalogService(),
                                null
                        )
                );
    }

    @Test
    void shouldCreateProviderService() {

        ProviderProfile profile = providerProfile();

        CreateProviderServiceRequest request =
                new CreateProviderServiceRequest(
                        serviceItemId,
                        new BigDecimal("25000.00")
                );

        ProviderService saved =
                new ProviderService(
                        providerProfileId,
                        serviceItemId,
                        request.price()
                );

        when(profileRepository.findByAuthUserId(
                authUserId.toString()
        )).thenReturn(Optional.of(profile));

        mockCatalogLookup();

        when(repository.existsByProviderProfileIdAndServiceItemId(
                providerProfileId,
                serviceItemId
        )).thenReturn(false);

        when(repository.save(any(ProviderService.class)))
                .thenReturn(saved);

        ProviderServiceResponse response =
                service.createService(jwt, request);

        assertNotNull(response);

        assertEquals(
                serviceItemId,
                response.serviceItemId()
        );

        assertEquals(
                new BigDecimal("25000.00"),
                response.price()
        );

        assertTrue(response.active());

        verify(repository).save(any(ProviderService.class));
    }

    @Test
    void shouldRejectDuplicateProviderService() {

        ProviderProfile profile = providerProfile();

        CreateProviderServiceRequest request =
                new CreateProviderServiceRequest(
                        serviceItemId,
                        new BigDecimal("25000.00")
                );

        when(profileRepository.findByAuthUserId(
                authUserId.toString()
        )).thenReturn(Optional.of(profile));

        mockCatalogLookup();

        when(repository.existsByProviderProfileIdAndServiceItemId(
                providerProfileId,
                serviceItemId
        )).thenReturn(true);

        assertThrows(
                DuplicateProviderServiceException.class,
                () -> service.createService(jwt, request)
        );

        verify(repository, never())
                .save(any(ProviderService.class));
    }

    @Test
    void shouldUpdateOwnedProviderService() {

        ProviderProfile profile = providerProfile();

        ProviderService providerService =
                new ProviderService(
                        providerProfileId,
                        serviceItemId,
                        new BigDecimal("20000.00")
                );

        UpdateProviderServiceRequest request =
                new UpdateProviderServiceRequest(
                        new BigDecimal("30000.00")
                );

        when(profileRepository.findByAuthUserId(
                authUserId.toString()
        )).thenReturn(Optional.of(profile));

        when(repository.findById(
                providerService.getId()
        )).thenReturn(Optional.of(providerService));

        mockCatalogLookup();

        ProviderServiceResponse response =
                service.updateService(
                        jwt,
                        providerService.getId(),
                        request
                );

        assertEquals(
                new BigDecimal("30000.00"),
                providerService.getPrice()
        );

        assertEquals(
                new BigDecimal("30000.00"),
                response.price()
        );

        verify(eventMapper)
                .toUpdatedEvent(providerService);

        verify(outboxPublisher)
                .publish(any());
    }

    @Test
    void shouldRejectUpdateWhenProviderDoesNotOwnService() {

        ProviderProfile profile = providerProfile();

        ProviderService providerService =
                new ProviderService(
                        UUID.randomUUID(),
                        serviceItemId,
                        new BigDecimal("20000.00")
                );

        when(profileRepository.findByAuthUserId(
                authUserId.toString()
        )).thenReturn(Optional.of(profile));

        when(repository.findById(
                providerService.getId()
        )).thenReturn(Optional.of(providerService));

        assertThrows(
                ProviderServiceOwnershipException.class,
                () -> service.updateService(
                        jwt,
                        providerService.getId(),
                        new UpdateProviderServiceRequest(
                                new BigDecimal("30000.00")
                        )
                )
        );

        verify(outboxPublisher, never())
                .publish(any());
    }

    @Test
    void shouldToggleOwnedProviderService() {

        ProviderProfile profile = providerProfile();

        ProviderService providerService =
                new ProviderService(
                        providerProfileId,
                        serviceItemId,
                        new BigDecimal("20000.00")
                );

        when(profileRepository.findByAuthUserId(
                authUserId.toString()
        )).thenReturn(Optional.of(profile));

        when(repository.findById(
                providerService.getId()
        )).thenReturn(Optional.of(providerService));

        mockCatalogLookup();

        assertTrue(providerService.isActive());

        ProviderServiceResponse response =
                service.toggleActive(
                        jwt,
                        providerService.getId()
                );

        assertFalse(providerService.isActive());
        assertFalse(response.active());

        verify(eventMapper)
                .toUpdatedEvent(providerService);

        verify(outboxPublisher)
                .publish(any());
    }

    @Test
    void shouldDeleteOwnedProviderService() {

        ProviderProfile profile = providerProfile();

        ProviderService providerService =
                new ProviderService(
                        providerProfileId,
                        serviceItemId,
                        new BigDecimal("20000.00")
                );

        when(profileRepository.findByAuthUserId(
                authUserId.toString()
        )).thenReturn(Optional.of(profile));

        when(repository.findById(
                providerService.getId()
        )).thenReturn(Optional.of(providerService));

        service.deleteService(
                jwt,
                providerService.getId()
        );

        verify(repository)
                .delete(providerService);

        verify(eventMapper)
                .toDeletedEvent(providerService);

        verify(outboxPublisher)
                .publish(any());
    }

    @Test
    void shouldReturnPublicProviderServices() {

        ProviderService providerService =
                new ProviderService(
                        providerProfileId,
                        serviceItemId,
                        new BigDecimal("20000.00")
                );

        when(repository.findByProviderProfileIdOrderByCreatedAtDesc(
                providerProfileId
        )).thenReturn(
                java.util.List.of(providerService)
        );

        mockCatalogLookup();

        var result =
                service.getProviderServices(
                        providerProfileId
                );

        assertEquals(1, result.size());

        assertEquals(
                serviceItemId,
                result.get(0).serviceItemId()
        );
    }

    @Test
    void shouldReturnServiceDetails() {

        ProviderService providerService =
                new ProviderService(
                        providerProfileId,
                        serviceItemId,
                        new BigDecimal("20000.00")
                );

        ProviderProfile profile =
                providerProfile();

        when(repository.findById(
                providerService.getId()
        )).thenReturn(Optional.of(providerService));

        when(profileRepository.findById(
                providerProfileId
        )).thenReturn(Optional.of(profile));

        mockCatalogLookup();

        ProviderServiceDetailsResponse response =
                service.getService(
                        providerService.getId()
                );

        assertEquals(
                providerService.getId(),
                response.id()
        );

        assertEquals(
                serviceItemId,
                response.serviceItemId()
        );

        assertEquals(
                "Pipe Repair",
                response.serviceName()
        );

        assertEquals(
                new BigDecimal("20000.00"),
                response.price()
        );

        assertTrue(response.active());
    }
}