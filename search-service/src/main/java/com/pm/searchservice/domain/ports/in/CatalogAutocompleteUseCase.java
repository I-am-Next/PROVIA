package com.pm.searchservice.domain.ports.in;

import com.pm.searchservice.domain.model.CatalogSuggestion;

import java.util.List;

public interface CatalogAutocompleteUseCase {

    List<CatalogSuggestion> autocomplete(String query);
}