package com.pm.servicecatalog.application.service;

import com.pm.contracts.catalog.ServiceItemCreatedEvent;
import com.pm.servicecatalog.application.dto.CreateServiceRequest;
import com.pm.servicecatalog.application.dto.ServiceItemResponse;
import com.pm.servicecatalog.application.event.DomainEventPublisher;
import com.pm.servicecatalog.domain.model.ServiceCategory;
import com.pm.servicecatalog.domain.model.ServiceItem;
import com.pm.servicecatalog.domain.ports.in.CreateServiceUseCase;
import com.pm.servicecatalog.domain.ports.out.ServiceCategoryRepository;
import com.pm.servicecatalog.domain.ports.out.ServiceItemRepository;
import com.pm.servicecatalog.shared.exception.CategoryNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class CreateServiceItemService
        implements CreateServiceUseCase {

    private final ServiceItemRepository serviceRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final DomainEventPublisher eventPublisher;

    public CreateServiceItemService(
            ServiceItemRepository serviceRepository,
            ServiceCategoryRepository categoryRepository,
            DomainEventPublisher eventPublisher
    ) {
        this.serviceRepository = serviceRepository;
        this.categoryRepository = categoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ServiceItemResponse createService(
            CreateServiceRequest request
    ) {

        ServiceCategory category =
                categoryRepository.findById(request.categoryId())
                        .orElseThrow(
                                () -> new CategoryNotFoundException(
                                        request.categoryId()
                                )
                        );

        /*
         * Keep the existing ServiceItem creation mechanism
         * from your domain model here.
         *
         * If your ServiceItem constructor is private, use
         * the existing factory method from ServiceItem.
         */
        ServiceItem serviceItem =
                ServiceItem.create(
                        request.name(),
                        request.description(),
                        request.categoryId()
                );

        ServiceItem saved =
                serviceRepository.save(serviceItem);

        ServiceItemCreatedEvent event =
                new ServiceItemCreatedEvent(
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