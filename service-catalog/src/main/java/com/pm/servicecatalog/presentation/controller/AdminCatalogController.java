package com.pm.servicecatalog.presentation.controller;

import com.pm.servicecatalog.application.dto.CategoryResponse;
import com.pm.servicecatalog.application.dto.CreateCategoryRequest;
import com.pm.servicecatalog.application.dto.CreateServiceRequest;
import com.pm.servicecatalog.application.dto.ServiceItemResponse;
import com.pm.servicecatalog.application.dto.UpdateCategoryRequest;
import com.pm.servicecatalog.application.dto.UpdateServiceRequest;
import com.pm.servicecatalog.domain.ports.in.*;
import com.pm.servicecatalog.shared.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/catalog/admin")
public class AdminCatalogController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;

    private final CreateServiceUseCase createServiceUseCase;
    private final UpdateServiceItemUseCase updateServiceUseCase;
    private final DeleteServiceItemUseCase deleteServiceUseCase;

    public AdminCatalogController(
            CreateCategoryUseCase createCategoryUseCase,
            UpdateCategoryUseCase updateCategoryUseCase,
            DeleteCategoryUseCase deleteCategoryUseCase,
            CreateServiceUseCase createServiceUseCase,
            UpdateServiceItemUseCase updateServiceUseCase,
            DeleteServiceItemUseCase deleteServiceUseCase
    ) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.updateCategoryUseCase = updateCategoryUseCase;
        this.deleteCategoryUseCase = deleteCategoryUseCase;
        this.createServiceUseCase = createServiceUseCase;
        this.updateServiceUseCase = updateServiceUseCase;
        this.deleteServiceUseCase = deleteServiceUseCase;
    }

    // =========================================================
    // CATEGORY
    // =========================================================

    @PostMapping("/categories")
    public ApiResponse<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {

        CategoryResponse response =
                createCategoryUseCase.createCategory(request);

        return ApiResponse.ok(response);
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {

        CategoryResponse response =
                updateCategoryUseCase.updateCategory(
                        id,
                        request
                );

        return ApiResponse.ok(response);
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(
            @PathVariable UUID id
    ) {

        deleteCategoryUseCase.deleteCategory(id);

        return ApiResponse.ok(null);
    }

    // =========================================================
    // SERVICE ITEM
    // =========================================================

    @PostMapping("/services")
    public ApiResponse<ServiceItemResponse> createService(
            @Valid @RequestBody CreateServiceRequest request
    ) {

        ServiceItemResponse response =
                createServiceUseCase.createService(request);

        return ApiResponse.ok(response);
    }

    @PutMapping("/services/{id}")
    public ApiResponse<ServiceItemResponse> updateService(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateServiceRequest request
    ) {

        ServiceItemResponse response =
                updateServiceUseCase.updateService(
                        id,
                        request
                );

        return ApiResponse.ok(response);
    }

    @DeleteMapping("/services/{id}")
    public ApiResponse<Void> deleteService(
            @PathVariable UUID id
    ) {

        deleteServiceUseCase.deleteService(id);

        return ApiResponse.ok(null);
    }
}