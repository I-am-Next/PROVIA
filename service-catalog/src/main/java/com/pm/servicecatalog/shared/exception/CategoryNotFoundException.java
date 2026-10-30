package com.pm.servicecatalog.shared.exception;

public class CategoryNotFoundException extends ApiException {

    public CategoryNotFoundException(Object categoryId) {
        super(
                "CATEGORY_NOT_FOUND",
                "Category not found with id: " + categoryId
        );
    }
}