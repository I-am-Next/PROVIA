package com.pm.servicecatalog.application.service;

import com.pm.servicecatalog.application.dto.CategoryResponse;
import com.pm.servicecatalog.application.dto.ServiceItemResponse;
import com.pm.servicecatalog.domain.model.ServiceCategory;
import com.pm.servicecatalog.domain.model.ServiceItem;
import com.pm.servicecatalog.domain.ports.in.GetCatalogUseCase;
import com.pm.servicecatalog.domain.ports.out.ServiceCategoryRepository;
import com.pm.servicecatalog.domain.ports.out.ServiceItemRepository;
import com.pm.servicecatalog.shared.exception.CategoryNotFoundException;
import com.pm.servicecatalog.shared.exception.ServiceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetCatalogService implements GetCatalogUseCase {

    private final ServiceCategoryRepository categoryRepository;
    private final ServiceItemRepository serviceRepository;

    public GetCatalogService(
            ServiceCategoryRepository categoryRepository,
            ServiceItemRepository serviceRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.serviceRepository = serviceRepository;
    }

    // =========================================================
    // GET ALL ACTIVE CATEGORIES
    // =========================================================

    @Override
    public List<CategoryResponse> getAllCategories() {

        return categoryRepository
                .findAllActive()
                .stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getDescription(),
                        category.isActive(),
                        List.of()
                ))
                .toList();
    }

    // =========================================================
    // GET ACTIVE CATEGORIES BY IDS
    // =========================================================

    @Override
    public List<CategoryResponse> getCategoriesByIds(
            List<UUID> ids
    ) {

        return categoryRepository
                .findAllActiveById(ids)
                .stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getDescription(),
                        category.isActive(),
                        List.of()
                ))
                .toList();
    }

    // =========================================================
    // GET ACTIVE CATEGORY WITH ACTIVE SERVICES
    // =========================================================

    @Override
    public CategoryResponse getCategoryWithServices(
            UUID categoryId
    ) {

        ServiceCategory category =
                categoryRepository
                        .findActiveById(categoryId)
                        .orElseThrow(
                                () -> new CategoryNotFoundException(
                                        categoryId
                                )
                        );

        List<ServiceItem> services =
                serviceRepository
                        .findByCategoryIdAndActiveTrue(categoryId);

        List<ServiceItemResponse> serviceResponses =
                services
                        .stream()
                        .map(service -> new ServiceItemResponse(
                                service.getId(),
                                service.getName(),
                                service.getDescription(),
                                service.getCategoryId(),
                                category.getName(),
                                service.isActive()
                        ))
                        .toList();

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive(),
                serviceResponses
        );
    }

    // =========================================================
    // GET ACTIVE SERVICE ITEM
    // =========================================================

    @Override
    public ServiceItemResponse getServiceById(
            UUID serviceId
    ) {

        ServiceItem serviceItem =
                serviceRepository
                        .findById(serviceId)
                        .orElseThrow(
                                () -> new ServiceNotFoundException(
                                        serviceId
                                )
                        );

        /*
         * Inactive service items should not be exposed
         * through the public catalog.
         */
        if (!serviceItem.isActive()) {
            throw new ServiceNotFoundException(serviceId);
        }

        ServiceCategory category =
                categoryRepository
                        .findActiveById(
                                serviceItem.getCategoryId()
                        )
                        .orElseThrow(
                                () -> new CategoryNotFoundException(
                                        serviceItem.getCategoryId()
                                )
                        );

        return new ServiceItemResponse(
                serviceItem.getId(),
                serviceItem.getName(),
                serviceItem.getDescription(),
                category.getId(),
                category.getName(),
                serviceItem.isActive()
        );
    }
}