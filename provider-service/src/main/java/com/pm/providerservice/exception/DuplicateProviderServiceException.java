package com.pm.providerservice.exception;

public class DuplicateProviderServiceException extends ApiException {

    public DuplicateProviderServiceException() {
        super(
                "DUPLICATE_PROVIDER_SERVICE",
                "Provider already offers this service"
        );
    }

}
