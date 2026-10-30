package com.pm.searchservice.domain.ports.in;

import com.pm.searchservice.domain.model.ProviderSearchCriteria;
import com.pm.searchservice.domain.model.ProviderSearchDocument;
import com.pm.searchservice.domain.model.SearchResult;

import java.util.Optional;
import java.util.UUID;

public interface SearchProvidersUseCase {

    SearchResult<ProviderSearchDocument> searchProviders(
            ProviderSearchCriteria criteria
    );

    Optional<ProviderSearchDocument> getProvider(
            UUID providerId
    );
}