package com.pm.searchservice.infrastructure.elasticsearch;

import co.elastic.clients.elasticsearch._types.FieldValue;
import com.pm.searchservice.domain.model.CatalogSuggestion;
import com.pm.searchservice.domain.ports.out.CatalogAutocompleteRepository;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CatalogAutocompleteRepositoryImpl
        implements CatalogAutocompleteRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    public CatalogAutocompleteRepositoryImpl(
            ElasticsearchOperations elasticsearchOperations
    ) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public List<CatalogSuggestion> searchCategories(
            String query,
            int limit
    ) {

        if (query == null || query.isBlank() || limit <= 0) {
            return List.of();
        }

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .filter(f -> f.term(t -> t
                                .field("active")
                                .value(FieldValue.of(true))
                        ))
                        .must(m -> m.matchPhrasePrefix(p -> p
                                .field("categoryName")
                                .query(query.trim())
                        ))
                ))
                .withMaxResults(limit)
                .build();

        SearchHits<CatalogCategorySearchDocumentEntity> hits =
                elasticsearchOperations.search(
                        searchQuery,
                        CatalogCategorySearchDocumentEntity.class
                );

        return hits.getSearchHits()
                .stream()
                .map(hit -> {
                    CatalogCategorySearchDocumentEntity document =
                            hit.getContent();

                    return new CatalogSuggestion(
                            CatalogSuggestion.SuggestionType.CATEGORY,
                            document.getCategoryId(),
                            document.getCategoryName(),
                            null,
                            null,
                            document.getDescription()
                    );
                })
                .toList();
    }

    @Override
    public List<CatalogSuggestion> searchServices(
            String query,
            int limit
    ) {

        if (query == null || query.isBlank() || limit <= 0) {
            return List.of();
        }

        NativeQuery searchQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .filter(f -> f.term(t -> t
                                .field("active")
                                .value(FieldValue.of(true))
                        ))
                        .should(s -> s.matchPhrasePrefix(p -> p
                                .field("serviceName")
                                .query(query.trim())
                        ))
                        .should(s -> s.matchPhrasePrefix(p -> p
                                .field("categoryName")
                                .query(query.trim())
                        ))
                        .minimumShouldMatch("1")
                ))
                .withMaxResults(limit)
                .build();

        SearchHits<CatalogServiceSearchDocumentEntity> hits =
                elasticsearchOperations.search(
                        searchQuery,
                        CatalogServiceSearchDocumentEntity.class
                );

        return hits.getSearchHits()
                .stream()
                .map(hit -> {
                    CatalogServiceSearchDocumentEntity document =
                            hit.getContent();

                    return new CatalogSuggestion(
                            CatalogSuggestion.SuggestionType.SERVICE,
                            document.getServiceItemId(),
                            document.getServiceName(),
                            document.getCategoryId(),
                            document.getCategoryName(),
                            document.getDescription()
                    );
                })
                .toList();
    }
}