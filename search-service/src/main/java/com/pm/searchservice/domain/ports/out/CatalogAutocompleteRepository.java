package com.pm.searchservice.domain.ports.out;

import com.pm.searchservice.domain.model.CatalogSuggestion;

import java.util.List;

public interface CatalogAutocompleteRepository {

    List<CatalogSuggestion> searchCategories(
            String query,
            int limit
    );

    List<CatalogSuggestion> searchServices(
            String query,
            int limit
    );
}