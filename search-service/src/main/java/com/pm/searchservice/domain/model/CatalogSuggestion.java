package com.pm.searchservice.domain.model;

import java.util.UUID;

public record CatalogSuggestion(
        SuggestionType type,
        UUID id,
        String name,
        UUID categoryId,
        String categoryName,
        String description
) {

    public enum SuggestionType {
        CATEGORY,
        SERVICE
    }
}