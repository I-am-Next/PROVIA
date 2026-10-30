package com.pm.searchservice.api.dto;

import java.util.List;
import java.util.UUID;

public record CatalogAutocompleteResponse(
        List<CatalogSuggestion> suggestions
) {

    public record CatalogSuggestion(
            String type,
            UUID id,
            String name,
            UUID categoryId,
            String categoryName,
            String description
    ) {
    }
}