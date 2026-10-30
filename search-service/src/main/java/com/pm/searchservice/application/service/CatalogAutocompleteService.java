package com.pm.searchservice.application.service;

import com.pm.searchservice.domain.model.CatalogSuggestion;
import com.pm.searchservice.domain.ports.in.CatalogAutocompleteUseCase;
import com.pm.searchservice.domain.ports.out.CatalogAutocompleteRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CatalogAutocompleteService
        implements CatalogAutocompleteUseCase {

    private static final int MAX_SUGGESTIONS = 10;
    private static final int MAX_RESULTS_PER_TYPE = 5;

    private final CatalogAutocompleteRepository repository;

    public CatalogAutocompleteService(
            CatalogAutocompleteRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public List<CatalogSuggestion> autocomplete(String query) {

        if (query == null || query.isBlank()) {
            return List.of();
        }

        String normalizedQuery = query.trim();

        List<CatalogSuggestion> categories =
                repository.searchCategories(
                        normalizedQuery,
                        MAX_RESULTS_PER_TYPE
                );

        List<CatalogSuggestion> services =
                repository.searchServices(
                        normalizedQuery,
                        MAX_RESULTS_PER_TYPE
                );

        List<CatalogSuggestion> suggestions =
                new ArrayList<>(MAX_SUGGESTIONS);

        /*
         * Alternate category and service suggestions so that
         * one result type does not automatically dominate.
         */
        int maxResults = Math.max(
                categories.size(),
                services.size()
        );

        for (int i = 0;
             i < maxResults && suggestions.size() < MAX_SUGGESTIONS;
             i++) {

            if (i < categories.size()
                    && suggestions.size() < MAX_SUGGESTIONS) {
                suggestions.add(categories.get(i));
            }

            if (i < services.size()
                    && suggestions.size() < MAX_SUGGESTIONS) {
                suggestions.add(services.get(i));
            }
        }

        return List.copyOf(suggestions);
    }
}