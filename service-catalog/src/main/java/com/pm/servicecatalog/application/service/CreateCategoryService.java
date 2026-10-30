package com.pm.servicecatalog.application.service;

import com.pm.contracts.catalog.CategoryCreatedEvent;
import com.pm.servicecatalog.application.dto.CategoryResponse;
import com.pm.servicecatalog.application.dto.CreateCategoryRequest;
import com.pm.servicecatalog.application.event.DomainEventPublisher;
import com.pm.servicecatalog.domain.model.ServiceCategory;
import com.pm.servicecatalog.domain.ports.in.CreateCategoryUseCase;
import com.pm.servicecatalog.domain.ports.out.ServiceCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CreateCategoryService
        implements CreateCategoryUseCase {

    private final ServiceCategoryRepository categoryRepository;
    private final DomainEventPublisher eventPublisher;

    public CreateCategoryService(
            ServiceCategoryRepository categoryRepository,
            DomainEventPublisher eventPublisher
    ) {
        this.categoryRepository = categoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CategoryResponse createCategory(
            CreateCategoryRequest request
    ) {

        /*
         * IMPORTANT:
         * Keep the existing domain creation mechanism from your
         * original CreateCategoryService here.
         *
         * Do NOT use:
         *
         * new ServiceCategory(...)
         *
         * because the domain constructor is private.
         */
        ServiceCategory category =
                ServiceCategory.create(
                        request.name(),
                        request.description()
                );

        ServiceCategory saved =
                categoryRepository.save(category);

        CategoryCreatedEvent event =
                new CategoryCreatedEvent(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        saved.getId(),
                        saved.getName(),
                        saved.getDescription(),
                        saved.isActive()
                );

        eventPublisher.publish(event);

        return new CategoryResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.isActive(),
                List.of()
        );
    }
}