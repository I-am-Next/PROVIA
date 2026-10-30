package com.pm.providerservice.application.service.impl;

import com.pm.contracts.provider.ProviderServiceCreatedEvent;
import com.pm.contracts.provider.ProviderServiceDeletedEvent;
import com.pm.contracts.provider.ProviderServiceUpdatedEvent;
import com.pm.providerservice.api.dto.request.CreateProviderServiceRequest;
import com.pm.providerservice.api.dto.request.UpdateProviderServiceRequest;
import com.pm.providerservice.api.dto.response.ProviderServiceDetailsResponse;
import com.pm.providerservice.api.dto.response.ProviderServiceResponse;
import com.pm.providerservice.application.ports.OutboxDomainEventPublisher;
import com.pm.providerservice.application.mapper.ProviderServiceEventMapper;
import com.pm.providerservice.client.CatalogClient;
import com.pm.providerservice.client.dto.ApiResponse;
import com.pm.providerservice.client.dto.CatalogServiceItemResponse;
import com.pm.providerservice.domain.ProviderProfile;
import com.pm.providerservice.domain.ProviderService;
import com.pm.providerservice.exception.CatalogLookupException;
import com.pm.providerservice.exception.DuplicateProviderServiceException;
import com.pm.providerservice.exception.ProviderProfileNotFoundException;
import com.pm.providerservice.exception.ProviderServiceNotFoundException;
import com.pm.providerservice.exception.ProviderServiceOwnershipException;
import com.pm.providerservice.repository.ProviderProfileRepository;
import com.pm.providerservice.repository.ProviderServiceRepository;
import feign.FeignException;
import jakarta.transaction.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProviderServiceServiceImpl
        implements com.pm.providerservice.application.service.ProviderServiceService {

    private final ProviderServiceRepository repository;
    private final ProviderProfileRepository profileRepository;
    private final CatalogClient catalogClient;
    private final ProviderServiceEventMapper eventMapper;
    private final OutboxDomainEventPublisher outboxDomainEventPublisher;

    public ProviderServiceServiceImpl(
            ProviderServiceRepository repository,
            ProviderProfileRepository profileRepository,
            CatalogClient catalogClient,
            ProviderServiceEventMapper eventMapper,
            OutboxDomainEventPublisher outboxDomainEventPublisher
    ) {
        this.repository = repository;
        this.profileRepository = profileRepository;
        this.catalogClient = catalogClient;
        this.eventMapper = eventMapper;
        this.outboxDomainEventPublisher = outboxDomainEventPublisher;
    }

    // =========================================================
    // CREATE SERVICE
    // =========================================================

    @Override
    public ProviderServiceResponse createService(
            Jwt jwt,
            CreateProviderServiceRequest request
    ) {

        ProviderProfile profile =
                profileRepository
                        .findByAuthUserId(jwt.getSubject())
                        .orElseThrow(
                                ProviderProfileNotFoundException::new
                        );

        getCatalogService(
                request.serviceItemId()
        );

        boolean alreadyExists =
                repository
                        .existsByProviderProfileIdAndServiceItemId(
                                profile.getId(),
                                request.serviceItemId()
                        );

        if (alreadyExists) {
            throw new DuplicateProviderServiceException();
        }

        ProviderService service =
                new ProviderService(
                        profile.getId(),
                        request.serviceItemId(),
                        request.price()
                );

        ProviderService saved =
                repository.save(service);

        ProviderServiceCreatedEvent event =
                eventMapper.toCreatedEvent(saved);

        outboxDomainEventPublisher.publish(event);

        return toResponse(saved);
    }

    // =========================================================
    // UPDATE SERVICE
    // =========================================================

    @Override
    public ProviderServiceResponse updateService(
            Jwt jwt,
            UUID serviceId,
            UpdateProviderServiceRequest request
    ) {

        ProviderService service =
                getOwnedService(
                        jwt,
                        serviceId
                );

        if (request.price() != null) {

            service.updatePrice(
                    request.price()
            );
        }

        ProviderServiceUpdatedEvent event =
                eventMapper.toUpdatedEvent(service);

        outboxDomainEventPublisher.publish(event);

        return toResponse(service);
    }

    // =========================================================
    // DELETE SERVICE
    // =========================================================

    @Override
    public void deleteService(
            Jwt jwt,
            UUID serviceId
    ) {

        ProviderService service =
                getOwnedService(
                        jwt,
                        serviceId
                );

        ProviderServiceDeletedEvent event =
                eventMapper.toDeletedEvent(service);

        repository.delete(service);

        outboxDomainEventPublisher.publish(event);
    }

    // =========================================================
    // TOGGLE SERVICE
    // =========================================================

    @Override
    public ProviderServiceResponse toggleActive(
            Jwt jwt,
            UUID serviceId
    ) {

        ProviderService service =
                getOwnedService(
                        jwt,
                        serviceId
                );

        service.toggle();

        ProviderServiceUpdatedEvent event =
                eventMapper.toUpdatedEvent(service);

        outboxDomainEventPublisher.publish(event);

        return toResponse(service);
    }

    // =========================================================
    // GET MY SERVICES
    // =========================================================

    @Override
    public List<ProviderServiceResponse> getMyServices(
            Jwt jwt
    ) {

        ProviderProfile profile =
                profileRepository
                        .findByAuthUserId(jwt.getSubject())
                        .orElseThrow(
                                ProviderProfileNotFoundException::new
                        );

        return repository
                .findByProviderProfileIdOrderByCreatedAtDesc(
                        profile.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET PROVIDER SERVICES
    // =========================================================

    @Override
    public List<ProviderServiceResponse> getProviderServices(
            UUID providerProfileId
    ) {

        return repository
                .findByProviderProfileIdOrderByCreatedAtDesc(
                        providerProfileId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET SERVICE DETAILS
    // =========================================================

    @Override
    public ProviderServiceDetailsResponse getService(
            UUID id
    ) {

        ProviderService service =
                repository
                        .findById(id)
                        .orElseThrow(
                                () -> new ProviderServiceNotFoundException(id)
                        );

        ProviderProfile profile =
                profileRepository
                        .findById(
                                service.getProviderProfileId()
                        )
                        .orElseThrow(
                                ProviderProfileNotFoundException::new
                        );

        CatalogServiceItemResponse catalog =
                getCatalogService(
                        service.getServiceItemId()
                );

        return new ProviderServiceDetailsResponse(
                service.getId(),
                service.getProviderProfileId(),
                UUID.fromString(
                        profile.getAuthUserId()
                ),
                service.getServiceItemId(),
                catalog.name(),
                catalog.description(),
                catalog.categoryName(),
                service.getPrice(),
                service.isActive()
        );
    }

    // =========================================================
    // GET OWNED SERVICE
    // =========================================================

    private ProviderService getOwnedService(
            Jwt jwt,
            UUID serviceId
    ) {

        ProviderProfile profile =
                profileRepository
                        .findByAuthUserId(jwt.getSubject())
                        .orElseThrow(
                                ProviderProfileNotFoundException::new
                        );

        ProviderService service =
                repository
                        .findById(serviceId)
                        .orElseThrow(
                                () -> new ProviderServiceNotFoundException(
                                        serviceId
                                )
                        );

        if (!service.getProviderProfileId()
                .equals(profile.getId())) {

            throw new ProviderServiceOwnershipException();
        }

        return service;
    }

    // =========================================================
    // MAP TO RESPONSE
    // =========================================================

    private ProviderServiceResponse toResponse(
            ProviderService service
    ) {

        CatalogServiceItemResponse catalog =
                getCatalogService(
                        service.getServiceItemId()
                );

        return new ProviderServiceResponse(
                service.getId(),
                service.getProviderProfileId(),
                service.getServiceItemId(),
                catalog.name(),
                catalog.description(),
                catalog.categoryName(),
                service.getPrice(),
                service.isActive(),
                service.getCreatedAt()
        );
    }

    // =========================================================
    // CATALOG LOOKUP
    // =========================================================

    private CatalogServiceItemResponse getCatalogService(
            UUID serviceItemId
    ) {

        try {

            ApiResponse<CatalogServiceItemResponse> response =
                    catalogClient.getServiceById(
                            serviceItemId
                    );

            if (!response.success()
                    || response.data() == null) {

                throw new CatalogLookupException(
                        "Catalog service returned empty response"
                );
            }

            return response.data();

        } catch (FeignException.NotFound ex) {

            throw new CatalogLookupException(
                    "Catalog service item not found: "
                            + serviceItemId
            );

        } catch (FeignException.Forbidden ex) {

            throw new CatalogLookupException(
                    "Catalog service access denied"
            );

        } catch (FeignException ex) {

            throw new CatalogLookupException(
                    "Catalog service unavailable"
            );
        }
    }
}