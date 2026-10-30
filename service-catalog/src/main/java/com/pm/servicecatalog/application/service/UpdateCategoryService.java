package com.pm.servicecatalog.application.service;

import com.pm.contracts.catalog.CategoryUpdatedEvent;
import com.pm.servicecatalog.application.dto.CategoryResponse;
import com.pm.servicecatalog.application.dto.UpdateCategoryRequest;
import com.pm.servicecatalog.application.event.DomainEventPublisher;
import com.pm.servicecatalog.domain.model.ServiceCategory;
import com.pm.servicecatalog.domain.ports.in.UpdateCategoryUseCase;
import com.pm.servicecatalog.domain.ports.out.ServiceCategoryRepository;
import com.pm.servicecatalog.shared.exception.CategoryNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UpdateCategoryService
        implements UpdateCategoryUseCase {

    private final ServiceCategoryRepository categoryRepository;
    private final DomainEventPublisher eventPublisher;

    public UpdateCategoryService(
            ServiceCategoryRepository categoryRepository,
            DomainEventPublisher eventPublisher
    ) {
        this.categoryRepository = categoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CategoryResponse updateCategory(
            UUID categoryId,
            UpdateCategoryRequest request
    ) {

        ServiceCategory category =
                categoryRepository.findById(categoryId)
                        .orElseThrow(
                                () -> new CategoryNotFoundException(categoryId)
                        );

        category.update(
                request.name(),
                request.description()
        );

        ServiceCategory saved =
                categoryRepository.save(category);

        CategoryUpdatedEvent event =
                new CategoryUpdatedEvent(
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