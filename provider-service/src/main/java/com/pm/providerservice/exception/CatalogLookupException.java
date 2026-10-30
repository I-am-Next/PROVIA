package com.pm.providerservice.exception;

public class CatalogLookupException  extends ApiException {

    public CatalogLookupException(String message) {
        super(
                "CATALOG_LOOKUP_FAILED",
                message
        );
    }
}
