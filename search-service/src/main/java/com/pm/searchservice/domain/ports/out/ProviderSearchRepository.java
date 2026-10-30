package com.pm.searchservice.domain.ports.out;

import com.pm.searchservice.domain.model.ProviderSearchCriteria;
import com.pm.searchservice.domain.model.ProviderSearchDocument;
import com.pm.searchservice.domain.model.SearchResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProviderSearchRepository {

    void save(
            ProviderSearchDocument document
    );

    Optional<ProviderSearchDocument> findByProviderId(
            UUID providerId
    );

    SearchResult<ProviderSearchDocument> search(
            ProviderSearchCriteria criteria
    );

    List<String> autocomplete(
            String query
    );

    List<ProviderSearchDocument> findByServiceCategoryId(
            UUID categoryId
    );

    void deleteByProviderId(
            UUID providerId
    );

    List<ProviderSearchDocument> findByServiceItemId(
            UUID serviceItemId
    );
}