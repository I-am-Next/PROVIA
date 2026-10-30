package com.pm.searchservice.api.dto.response;

import com.pm.searchservice.domain.model.ProviderSearchDocument;
import com.pm.searchservice.domain.model.SearchResult;

import java.util.List;

public record ProviderSearchPageResponse(
        List<ProviderSearchResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static ProviderSearchPageResponse from(
            SearchResult<ProviderSearchDocument> result
    ) {

        List<ProviderSearchResponse> content =
                result.content()
                        .stream()
                        .map(ProviderSearchResponse::from)
                        .toList();

        return new ProviderSearchPageResponse(
                content,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages()
        );
    }
}