package com.pm.servicecatalog.shared.exception;

public class DuplicateCategoryException extends ApiException {

    public DuplicateCategoryException(String name) {
        super("CATEGORY_ALREADY_EXISTS", "Category already exists: " + name);
    }

}
