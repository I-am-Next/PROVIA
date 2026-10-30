package com.pm.servicecatalog.application.service;

import com.pm.contracts.catalog.ServiceItemUpdatedEvent;
import com.pm.servicecatalog.application.dto.ServiceItemResponse;
import com.pm.servicecatalog.application.dto.UpdateServiceRequest;
import com.pm.servicecatalog.application.event.DomainEventPublisher;
import com.pm.servicecatalog.domain.model.ServiceCategory;
import com.pm.servicecatalog.domain.model.ServiceItem;
import com.pm.servicecatalog.domain.ports.in.UpdateServiceItemUseCase;
import com.pm.servicecatalog.domain.ports.out.ServiceCategoryRepository;
import com.pm.servicecatalog.domain.ports.out.ServiceItemRepository;
import com.pm.servicecatalog.shared.exception.CategoryNotFoundException;
import com.pm.servicecatalog.shared.exception.ServiceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class UpdateServiceItemService
        implements UpdateServiceItemUseCase {

    private final ServiceItemRepository serviceRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final DomainEventPublisher eventPublisher;

    public UpdateServiceItemService(
            ServiceItemRepository serviceRepository,
            ServiceCategoryRepository categoryRepository,
            DomainEventPublisher eventPublisher
    ) {
        this.serviceRepository = serviceRepository;
        this.categoryRepository = categoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ServiceItemResponse updateService(
            UUID serviceId,
            UpdateServiceRequest request
    ) {

        ServiceItem serviceItem =
                serviceRepository.findById(serviceId)
                        .orElseThrow(
                                () -> new ServiceNotFoundException(serviceId)
                        );

        ServiceCategory category =
                categoryRepository.findById(request.categoryId())
                        .orElseThrow(
                                () -> new CategoryNotFoundException(
                                        request.categoryId()
                                )
                        );

        serviceItem.update(
                request.name(),
                request.description(),
                request.categoryId()
        );

        ServiceItem saved =
                serviceRepository.save(serviceItem);

        ServiceItemUpdatedEvent event =
                new ServiceItemUpdatedEvent(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        saved.getId(),
                        saved.getCategoryId(),
                        saved.getName(),
                        saved.getDescription(),
                        saved.isActive()
                );

        eventPublisher.publish(event);

        return new ServiceItemResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getCategoryId(),
                category.getName(),
                saved.isActive()
        );
    }
}