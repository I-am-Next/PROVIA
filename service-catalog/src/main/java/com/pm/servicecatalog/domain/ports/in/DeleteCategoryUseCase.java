package com.pm.servicecatalog.domain.ports.in;

import java.util.UUID;

public interface DeleteCategoryUseCase {

    void deleteCategory(UUID categoryId);
}