package com.pm.providerservice.exception;

import java.util.UUID;

public class ProviderServiceNotFoundException  extends ApiException {

    public ProviderServiceNotFoundException(UUID id) {
        super(
                "PROVIDER_SERVICE_NOT_FOUND",
                "Provider service not found: " + id
        );
    }
}
