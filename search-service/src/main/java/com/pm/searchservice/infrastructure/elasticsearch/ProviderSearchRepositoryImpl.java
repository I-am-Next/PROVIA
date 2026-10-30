package com.pm.searchservice.infrastructure.elasticsearch;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.json.JsonData;
import com.pm.searchservice.domain.model.ProviderSearchCriteria;
import com.pm.searchservice.domain.model.ProviderSearchDocument;
import com.pm.searchservice.domain.model.SearchResult;
import com.pm.searchservice.domain.ports.out.ProviderSearchRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ProviderSearchRepositoryImpl
        implements ProviderSearchRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    public ProviderSearchRepositoryImpl(
            ElasticsearchOperations elasticsearchOperations
    ) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    // =========================================================
    // SAVE
    // =========================================================

    @Override
    public void save(ProviderSearchDocument document) {

        ProviderSearchDocumentEntity entity = toEntity(document);

        elasticsearchOperations.save(entity);
    }

    // =========================================================
    // FIND BY PROVIDER ID
    // =========================================================

    @Override
    public Optional<ProviderSearchDocument> findByProviderId(
            UUID providerId
    ) {

        ProviderSearchDocumentEntity entity =
                elasticsearchOperations.get(
                        providerId.toString(),
                        ProviderSearchDocumentEntity.class
                );

        if (entity == null) {
            return Optional.empty();
        }

        return Optional.of(toDomain(entity));
    }

    @Override
    public List<ProviderSearchDocument> findByServiceItemId(
            UUID serviceItemId
    ) {

        NativeQuery searchQuery =
                NativeQuery.builder()
                        .withQuery(
                                q -> q.nested(nested -> nested
                                        .path("services")
                                        .query(
                                                nestedQuery ->
                                                        nestedQuery.term(t -> t
                                                                .field(
                                                                        "services.serviceItemId"
                                                                )
                                                                .value(
                                                                        serviceItemId.toString()
                                                                )
                                                        )
                                        )
                                )
                        )
                        .build();

        SearchHits<ProviderSearchDocumentEntity> hits =
                elasticsearchOperations.search(
                        searchQuery,
                        ProviderSearchDocumentEntity.class
                );

        return hits.getSearchHits()
                .stream()
                .map(hit -> toDomain(hit.getContent()))
                .toList();
    }

    // =========================================================
    // SEARCH
    // =========================================================

    @Override
    public SearchResult<ProviderSearchDocument> search(
            ProviderSearchCriteria criteria
    ) {

        NativeQueryBuilder queryBuilder =
                NativeQuery.builder()
                        .withQuery(q -> q.bool(bool -> {

                            // =================================================
                            // TEXT SEARCH
                            // =================================================

                            if (hasText(criteria.query())) {

                                bool.should(
                                        s -> s.multiMatch(m -> m
                                                .query(criteria.query())
                                                .fields(
                                                        "providerName^4",
                                                        "description^2"
                                                )
                                                .fuzziness("AUTO")
                                        )
                                );

                                bool.should(
                                        s -> s.nested(nested -> nested
                                                .path("services")
                                                .query(
                                                        nestedQuery ->
                                                                nestedQuery.multiMatch(m -> m
                                                                        .query(criteria.query())
                                                                        .fields(
                                                                                "services.serviceName^4",
                                                                                "services.categoryName^3"
                                                                        )
                                                                        .fuzziness("AUTO")
                                                                )
                                                )
                                        )
                                );

                                bool.minimumShouldMatch("1");
                            }

                            // =================================================
                            // ACTIVE PROVIDER FILTER
                            // =================================================
                            //
                            // A provider is considered active/searchable when
                            // it has at least one active provider service.
                            // =================================================

                            if (Boolean.TRUE.equals(criteria.active())) {

                                bool.filter(
                                        f -> f.nested(nested -> nested
                                                .path("services")
                                                .query(
                                                        nestedQuery ->
                                                                nestedQuery.term(t -> t
                                                                        .field("services.active")
                                                                        .value(true)
                                                                )
                                                )
                                        )
                                );
                            }

                            // =================================================
                            // VERIFIED FILTER
                            // =================================================

                            if (criteria.verified() != null) {

                                bool.filter(
                                        f -> f.term(t -> t
                                                .field("verified")
                                                .value(criteria.verified())
                                        )
                                );
                            }

                            // =================================================
                            // MINIMUM RATING FILTER
                            // =================================================

                            if (criteria.minRating() != null) {

                                bool.filter(
                                        f -> f.range(r -> r
                                                .field("rating")
                                                .gte(
                                                        JsonData.of(
                                                                criteria.minRating()
                                                        )
                                                )
                                        )
                                );
                            }

                            // =================================================
                            // SERVICE FILTERS
                            // =================================================
                            //
                            // These remain inside ONE nested query so that
                            // category, service and price all apply to the
                            // same provider service.
                            // =================================================

                            if (hasServiceFilters(criteria)) {

                                bool.filter(
                                        f -> f.nested(nested -> nested
                                                .path("services")
                                                .query(
                                                        nestedQuery ->
                                                                nestedQuery.bool(serviceBool -> {

                                                                    // =============================================
                                                                    // CATEGORY
                                                                    // =============================================

                                                                    if (criteria.categoryId() != null) {

                                                                        serviceBool.filter(
                                                                                sf -> sf.term(t -> t
                                                                                        .field(
                                                                                                "services.categoryId"
                                                                                        )
                                                                                        .value(
                                                                                                criteria.categoryId()
                                                                                                        .toString()
                                                                                        )
                                                                                )
                                                                        );
                                                                    }

                                                                    // =============================================
                                                                    // SERVICE ITEM
                                                                    // =============================================

                                                                    if (criteria.serviceItemId() != null) {

                                                                        serviceBool.filter(
                                                                                sf -> sf.term(t -> t
                                                                                        .field(
                                                                                                "services.serviceItemId"
                                                                                        )
                                                                                        .value(
                                                                                                criteria.serviceItemId()
                                                                                                        .toString()
                                                                                        )
                                                                                )
                                                                        );
                                                                    }

                                                                    // =============================================
                                                                    // MIN PRICE
                                                                    // =============================================

                                                                    if (criteria.minPrice() != null) {

                                                                        serviceBool.filter(
                                                                                sf -> sf.range(r -> r
                                                                                        .field(
                                                                                                "services.price"
                                                                                        )
                                                                                        .gte(
                                                                                                JsonData.of(
                                                                                                        criteria.minPrice()
                                                                                                )
                                                                                        )
                                                                                )
                                                                        );
                                                                    }

                                                                    // =============================================
                                                                    // MAX PRICE
                                                                    // =============================================

                                                                    if (criteria.maxPrice() != null) {

                                                                        serviceBool.filter(
                                                                                sf -> sf.range(r -> r
                                                                                        .field(
                                                                                                "services.price"
                                                                                        )
                                                                                        .lte(
                                                                                                JsonData.of(
                                                                                                        criteria.maxPrice()
                                                                                                )
                                                                                        )
                                                                                )
                                                                        );
                                                                    }

                                                                    return serviceBool;
                                                                })
                                                )
                                        )
                                );
                            }

                            return bool;
                        }))
                        .withPageable(
                                PageRequest.of(
                                        criteria.page(),
                                        criteria.size()
                                )
                        );

        // =========================================================
        // SORTING
        // =========================================================

        applySort(queryBuilder, criteria);

        // =========================================================
        // BUILD QUERY
        // =========================================================

        NativeQuery searchQuery =
                queryBuilder.build();

        // =========================================================
        // EXECUTE SEARCH
        // =========================================================

        SearchHits<ProviderSearchDocumentEntity> hits =
                elasticsearchOperations.search(
                        searchQuery,
                        ProviderSearchDocumentEntity.class
                );

        // =========================================================
        // MAP RESULTS
        // =========================================================

        List<ProviderSearchDocument> content =
                hits.getSearchHits()
                        .stream()
                        .map(hit -> toDomain(hit.getContent()))
                        .toList();

        // =========================================================
        // PAGINATION
        // =========================================================

        long totalElements =
                hits.getTotalHits();

        int totalPages =
                totalElements == 0
                        ? 0
                        : (int) Math.ceil(
                        (double) totalElements
                                / criteria.size()
                );

        return new SearchResult<>(
                content,
                criteria.page(),
                criteria.size(),
                totalElements,
                totalPages
        );
    }

    // =========================================================
    // FIND PROVIDERS BY SERVICE CATEGORY
    // =========================================================

    @Override
    public List<ProviderSearchDocument> findByServiceCategoryId(
            UUID categoryId
    ) {

        NativeQuery searchQuery =
                NativeQuery.builder()
                        .withQuery(
                                q -> q.nested(nested -> nested
                                        .path("services")
                                        .query(
                                                nestedQuery ->
                                                        nestedQuery.term(t -> t
                                                                .field(
                                                                        "services.categoryId"
                                                                )
                                                                .value(
                                                                        categoryId.toString()
                                                                )
                                                        )
                                        )
                                )
                        )
                        .build();

        SearchHits<ProviderSearchDocumentEntity> hits =
                elasticsearchOperations.search(
                        searchQuery,
                        ProviderSearchDocumentEntity.class
                );

        return hits.getSearchHits()
                .stream()
                .map(hit -> toDomain(hit.getContent()))
                .toList();
    }

    // =========================================================
    // AUTOCOMPLETE
    // =========================================================

    @Override
    public List<String> autocomplete(String query) {

        if (!hasText(query)) {
            return List.of();
        }

        NativeQuery searchQuery =
                NativeQuery.builder()
                        .withQuery(
                                q -> q.bool(bool -> {

                                    // =========================================
                                    // PROVIDER NAME
                                    // =========================================

                                    bool.should(
                                            s -> s.matchPhrasePrefix(m -> m
                                                    .field("providerName")
                                                    .query(query.trim())
                                            )
                                    );

                                    // =========================================
                                    // SERVICE NAME
                                    // =========================================

                                    bool.should(
                                            s -> s.nested(nested -> nested
                                                    .path("services")
                                                    .query(
                                                            nestedQuery ->
                                                                    nestedQuery.matchPhrasePrefix(m -> m
                                                                            .field(
                                                                                    "services.serviceName"
                                                                            )
                                                                            .query(query.trim())
                                                                    )
                                                    )
                                            )
                                    );

                                    // =========================================
                                    // CATEGORY NAME
                                    // =========================================

                                    bool.should(
                                            s -> s.nested(nested -> nested
                                                    .path("services")
                                                    .query(
                                                            nestedQuery ->
                                                                    nestedQuery.matchPhrasePrefix(m -> m
                                                                            .field(
                                                                                    "services.categoryName"
                                                                            )
                                                                            .query(query.trim())
                                                                    )
                                                    )
                                            )
                                    );

                                    bool.minimumShouldMatch("1");

                                    return bool;
                                })
                        )
                        .withMaxResults(10)
                        .build();

        SearchHits<ProviderSearchDocumentEntity> hits =
                elasticsearchOperations.search(
                        searchQuery,
                        ProviderSearchDocumentEntity.class
                );

        return hits.getSearchHits()
                .stream()
                .map(hit ->
                        hit.getContent().getProviderName()
                )
                .distinct()
                .limit(10)
                .toList();
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void deleteByProviderId(UUID providerId) {

        elasticsearchOperations.delete(
                providerId.toString(),
                ProviderSearchDocumentEntity.class
        );
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private boolean hasText(String value) {

        return value != null
                && !value.isBlank();
    }

    private boolean hasServiceFilters(
            ProviderSearchCriteria criteria
    ) {

        return criteria.categoryId() != null
                || criteria.serviceItemId() != null
                || criteria.minPrice() != null
                || criteria.maxPrice() != null;
    }

    private void applySort(
            NativeQueryBuilder queryBuilder,
            ProviderSearchCriteria criteria
    ) {

        String sort = criteria.sort();

        switch (sort) {

            case "rating" -> {

                queryBuilder.withSort(
                        SortOptions.of(s -> s
                                .field(f -> f
                                        .field("rating")
                                        .order(SortOrder.Desc)
                                        .missing("_last")
                                )
                        )
                );

                queryBuilder.withSort(
                        SortOptions.of(s -> s
                                .field(f -> f
                                        .field("reviewCount")
                                        .order(SortOrder.Desc)
                                )
                        )
                );
            }

            case "price_low" ->

                queryBuilder.withSort(
                        SortOptions.of(s -> s
                                .field(f -> f
                                        .field("services.price")
                                        .order(SortOrder.Asc)
                                        .nested(n -> n
                                                .path("services")
                                        )
                                )
                        )
                );


            case "price_high" ->

                queryBuilder.withSort(
                        SortOptions.of(s -> s
                                .field(f -> f
                                        .field("services.price")
                                        .order(SortOrder.Desc)
                                        .nested(n -> n
                                                .path("services")
                                        )
                                )
                        )
                );

            case "relevance" -> {
                // Elasticsearch relevance score.
            }

            default -> {
                // Unknown sort values use relevance.
            }
        }
    }

    // =========================================================
    // DOMAIN → ELASTICSEARCH
    // =========================================================


    private ProviderSearchDocumentEntity toEntity(
            ProviderSearchDocument document
    ) {

        List<ProviderSearchDocumentEntity.ServiceSearchItem> services =
                new ArrayList<>();

        if (document.getServices() != null) {

            for (ProviderSearchDocument.ServiceSearchItem service :
                    document.getServices()) {

                services.add(
                        new ProviderSearchDocumentEntity.ServiceSearchItem(
                                service.providerServiceId(),
                                service.serviceItemId(),
                                service.serviceName(),
                                service.categoryId(),
                                service.categoryName(),
                                service.price(),
                                service.active()
                        )
                );
            }
        }

        return new ProviderSearchDocumentEntity(
                document.getProviderId(),
                document.getProviderName(),
                document.getDescription(),
                document.getLocation(),
                document.getRating(),
                document.getReviewCount(),
                document.isVerified(),
                document.getProfilePhotoMediaId(),
                services
        );
    }



    // =========================================================
    // ELASTICSEARCH → DOMAIN
    // =========================================================


    private ProviderSearchDocument toDomain(
            ProviderSearchDocumentEntity entity
    ) {

        List<ProviderSearchDocument.ServiceSearchItem> services =
                new ArrayList<>();

        if (entity.getServices() != null) {

            for (ProviderSearchDocumentEntity.ServiceSearchItem service :
                    entity.getServices()) {

                services.add(
                        new ProviderSearchDocument.ServiceSearchItem(
                                service.getProviderServiceId(),
                                service.getServiceItemId(),
                                service.getServiceName(),
                                service.getCategoryId(),
                                service.getCategoryName(),
                                service.getPrice(),
                                service.isActive()
                        )
                );
            }
        }

        return new ProviderSearchDocument(
                entity.getProviderId(),
                entity.getProviderName(),
                entity.getDescription(),
                entity.getLocation(),
                entity.getRating(),
                entity.getReviewCount(),
                entity.isVerified(),
                entity.getProfilePhotoMediaId(),
                services
        );
    }

}