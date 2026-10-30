package com.pm.servicecatalog.presentation.controller;

import com.pm.servicecatalog.application.dto.CategoryResponse;
import com.pm.servicecatalog.application.dto.ServiceItemResponse;
import com.pm.servicecatalog.domain.ports.in.GetCatalogUseCase;
import com.pm.servicecatalog.shared.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/catalog")
public class CatalogController {

    private final GetCatalogUseCase getCatalogUseCase;

    public CatalogController(
            GetCatalogUseCase getCatalogUseCase
    ) {
        this.getCatalogUseCase = getCatalogUseCase;
    }

    // =========================================================
    // PUBLIC CATEGORIES
    // =========================================================

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> getAllCategories() {

        List<CategoryResponse> categories =
                getCatalogUseCase.getAllCategories();

        return ApiResponse.ok(categories);
    }

    @GetMapping("/categories/{id}")
    public ApiResponse<CategoryResponse> getCategoryWithServices(
            @PathVariable UUID id
    ) {

        CategoryResponse category =
                getCatalogUseCase.getCategoryWithServices(id);

        return ApiResponse.ok(category);
    }

    @GetMapping("/categories/batch")
    public ApiResponse<List<CategoryResponse>> getCategoriesByIds(
            @RequestParam List<UUID> ids
    ) {

        List<CategoryResponse> categories =
                getCatalogUseCase.getCategoriesByIds(ids);

        return ApiResponse.ok(categories);
    }

    // =========================================================
    // PUBLIC SERVICE ITEMS
    // =========================================================

    @GetMapping("/services/{id}")
    public ApiResponse<ServiceItemResponse> getService(
            @PathVariable UUID id
    ) {

        ServiceItemResponse service =
                getCatalogUseCase.getServiceById(id);

        return ApiResponse.ok(service);
    }
}