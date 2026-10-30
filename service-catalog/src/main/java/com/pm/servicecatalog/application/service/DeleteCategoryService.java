package com.pm.servicecatalog.application.service;

import com.pm.contracts.catalog.CategoryDeletedEvent;
import com.pm.servicecatalog.application.event.DomainEventPublisher;
import com.pm.servicecatalog.domain.model.ServiceCategory;
import com.pm.servicecatalog.domain.ports.in.DeleteCategoryUseCase;
import com.pm.servicecatalog.domain.ports.out.ServiceCategoryRepository;
import com.pm.servicecatalog.shared.exception.CategoryNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class DeleteCategoryService
        implements DeleteCategoryUseCase {

    private final ServiceCategoryRepository categoryRepository;
    private final DomainEventPublisher eventPublisher;

    public DeleteCategoryService(
            ServiceCategoryRepository categoryRepository,
            DomainEventPublisher eventPublisher
    ) {
        this.categoryRepository = categoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void deleteCategory(
            UUID categoryId
    ) {

        ServiceCategory category =
                categoryRepository.findById(categoryId)
                        .orElseThrow(
                                () -> new CategoryNotFoundException(categoryId)
                        );

        category.deactivate();

        categoryRepository.save(category);

        CategoryDeletedEvent event =
                new CategoryDeletedEvent(
                        UUID.randomUUID(),
                        1,
                        Instant.now(),
                        category.getId()
                );

        eventPublisher.publish(event);
    }
}