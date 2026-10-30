package com.pm.searchservice.api.mapper;

import com.pm.searchservice.api.dto.CatalogAutocompleteResponse;
import com.pm.searchservice.domain.model.CatalogSuggestion;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CatalogAutocompleteMapper {

    public CatalogAutocompleteResponse toResponse(
            List<CatalogSuggestion> suggestions
    ) {

        List<CatalogAutocompleteResponse.CatalogSuggestion> mapped =
                suggestions.stream()
                        .map(this::toSuggestion)
                        .toList();

        return new CatalogAutocompleteResponse(mapped);
    }

    private CatalogAutocompleteResponse.CatalogSuggestion toSuggestion(
            CatalogSuggestion suggestion
    ) {

        return new CatalogAutocompleteResponse.CatalogSuggestion(
                suggestion.type().name(),
                suggestion.id(),
                suggestion.name(),
                suggestion.categoryId(),
                suggestion.categoryName(),
                suggestion.description()
        );
    }
}