package com.pm.providerservice.exception;

public class ProviderServiceOwnershipException extends ApiException {

    public ProviderServiceOwnershipException() {
        super(
                "PROVIDER_SERVICE_FORBIDDEN",
                "You do not own this service"
        );
    }
}
