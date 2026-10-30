package com.pm.providerservice.exception;

public class ProviderProfileNotFoundException  extends ApiException {

    public ProviderProfileNotFoundException() {
        super(
                "PROVIDER_PROFILE_NOT_FOUND",
                "Provider profile not found"
        );
    }
}
