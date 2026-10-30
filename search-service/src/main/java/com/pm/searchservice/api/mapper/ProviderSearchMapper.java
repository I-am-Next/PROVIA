package com.pm.searchservice.api.mapper;

import com.pm.searchservice.api.dto.AutocompleteResponse;
import com.pm.searchservice.api.dto.request.ProviderSearchRequest;
import com.pm.searchservice.api.dto.response.ProviderSearchPageResponse;
import com.pm.searchservice.api.dto.response.ProviderSearchResponse;
import com.pm.searchservice.domain.model.ProviderSearchCriteria;
import com.pm.searchservice.domain.model.ProviderSearchDocument;
import com.pm.searchservice.domain.model.SearchResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProviderSearchMapper {

    public ProviderSearchCriteria toCriteria(
            ProviderSearchRequest request
    ) {

        return new ProviderSearchCriteria(
                request.query(),
                request.categoryId(),
                request.serviceItemId(),
                request.minPrice(),
                request.maxPrice(),
                request.minRating(),
                request.verified(),
                request.active(),
                request.page(),
                request.size(),
                request.sort()
        );
    }

    public ProviderSearchResponse toResponse(
            ProviderSearchDocument document
    ) {

        return ProviderSearchResponse.from(document);
    }

    public ProviderSearchPageResponse toPageResponse(
            SearchResult<ProviderSearchDocument> result
    ) {

        return ProviderSearchPageResponse.from(result);
    }

    public AutocompleteResponse toAutocompleteResponse(
            List<String> suggestions
    ) {

        return new AutocompleteResponse(
                suggestions == null ? List.of() : List.copyOf(suggestions)
        );
    }
}
