package com.pm.servicecatalog.domain.ports.in;

import com.pm.servicecatalog.application.dto.CategoryResponse;
import com.pm.servicecatalog.application.dto.ServiceItemResponse;

import java.util.List;
import java.util.UUID;

public interface GetCatalogUseCase {
    List<CategoryResponse> getAllCategories();

    List<CategoryResponse> getCategoriesByIds(List<UUID> ids);

    CategoryResponse getCategoryWithServices(UUID categoryId);

    ServiceItemResponse getServiceById(UUID serviceId);

}
