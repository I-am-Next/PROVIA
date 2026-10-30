package com.pm.searchservice.application.service;

import com.pm.searchservice.domain.model.ProviderSearchCriteria;
import com.pm.searchservice.domain.model.ProviderSearchDocument;
import com.pm.searchservice.domain.model.SearchResult;
import com.pm.searchservice.domain.ports.in.SearchProvidersUseCase;
import com.pm.searchservice.domain.ports.out.ProviderSearchRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class SearchProvidersService
        implements SearchProvidersUseCase {

    private final ProviderSearchRepository providerSearchRepository;

    public SearchProvidersService(
            ProviderSearchRepository providerSearchRepository
    ) {
        this.providerSearchRepository =
                providerSearchRepository;
    }

    @Override
    public SearchResult<ProviderSearchDocument> searchProviders(
            ProviderSearchCriteria criteria
    ) {

        return providerSearchRepository.search(
                criteria
        );
    }

    @Override
    public Optional<ProviderSearchDocument> getProvider(
            UUID providerId
    ) {

        return providerSearchRepository.findByProviderId(
                providerId
        );
    }
}