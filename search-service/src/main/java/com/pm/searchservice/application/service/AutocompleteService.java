package com.pm.searchservice.application.service;

import com.pm.searchservice.domain.ports.in.AutocompleteUseCase;
import com.pm.searchservice.domain.ports.out.ProviderSearchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutocompleteService
        implements AutocompleteUseCase {

    private final ProviderSearchRepository repository;

    public AutocompleteService(
            ProviderSearchRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public List<String> autocomplete(String query) {

        if (query == null || query.isBlank()) {
            return List.of();
        }

        return repository.autocomplete(query.trim());
    }
}