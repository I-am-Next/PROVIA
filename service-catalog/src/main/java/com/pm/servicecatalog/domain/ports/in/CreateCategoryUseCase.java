package com.pm.servicecatalog.domain.ports.in;

import com.pm.servicecatalog.application.dto.CategoryResponse;
import com.pm.servicecatalog.application.dto.CreateCategoryRequest;

public interface CreateCategoryUseCase {

    CategoryResponse createCategory(CreateCategoryRequest request);

}