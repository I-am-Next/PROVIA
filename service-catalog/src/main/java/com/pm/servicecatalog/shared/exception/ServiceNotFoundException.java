package com.pm.servicecatalog.shared.exception;

public class ServiceNotFoundException extends ApiException {

    public ServiceNotFoundException(Object id) {
        super(
                "SERVICE_NOT_FOUND",
                "Service not found with id: " + id
        );
    }
}