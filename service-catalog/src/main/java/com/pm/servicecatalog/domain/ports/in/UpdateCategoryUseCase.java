package com.pm.servicecatalog.domain.ports.in;

import com.pm.servicecatalog.application.dto.CategoryResponse;
import com.pm.servicecatalog.application.dto.UpdateCategoryRequest;

import java.util.UUID;

public interface UpdateCategoryUseCase {

    CategoryResponse updateCategory(
            UUID categoryId,
            UpdateCategoryRequest request
    );
}