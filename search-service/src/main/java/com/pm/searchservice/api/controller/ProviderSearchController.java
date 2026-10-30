package com.pm.searchservice.api.controller;

import com.pm.searchservice.api.dto.request.ProviderSearchRequest;
import com.pm.searchservice.api.dto.response.ProviderSearchPageResponse;
import com.pm.searchservice.api.dto.response.ProviderSearchResponse;
import com.pm.searchservice.api.mapper.ProviderSearchMapper;
import com.pm.searchservice.domain.model.ProviderSearchDocument;
import com.pm.searchservice.domain.model.SearchResult;
import com.pm.searchservice.domain.ports.in.SearchProvidersUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/search/providers")
public class ProviderSearchController {

    private final SearchProvidersUseCase searchProvidersUseCase;
    private final ProviderSearchMapper providerSearchMapper;

    public ProviderSearchController(
            SearchProvidersUseCase searchProvidersUseCase,
            ProviderSearchMapper providerSearchMapper
    ) {
        this.searchProvidersUseCase = searchProvidersUseCase;
        this.providerSearchMapper = providerSearchMapper;
    }

    // =========================================================
    // SEARCH PROVIDERS
    // =========================================================

    @GetMapping
    public ResponseEntity<ProviderSearchPageResponse> searchProviders(

            @RequestParam(required = false)
            String query,

            @RequestParam(required = false)
            UUID categoryId,

            @RequestParam(required = false)
            UUID serviceItemId,

            @RequestParam(required = false)
            BigDecimal minPrice,

            @RequestParam(required = false)
            BigDecimal maxPrice,

            @RequestParam(required = false)
            BigDecimal minRating,

            @RequestParam(required = false)
            Boolean verified,

            @RequestParam(required = false)
            Boolean active,

            @RequestParam(required = false, defaultValue = "0")
            Integer page,

            @RequestParam(required = false, defaultValue = "20")
            Integer size,

            @RequestParam(required = false, defaultValue = "relevance")
            String sort
    ) {

        ProviderSearchRequest request =
                new ProviderSearchRequest(
                        query,
                        categoryId,
                        serviceItemId,
                        minPrice,
                        maxPrice,
                        minRating,
                        verified,
                        active,
                        page,
                        size,
                        sort
                );

        var criteria =
                providerSearchMapper.toCriteria(request);

        SearchResult<ProviderSearchDocument> result =
                searchProvidersUseCase.searchProviders(criteria);

        return ResponseEntity.ok(
                providerSearchMapper.toPageResponse(result)
        );
    }

    // =========================================================
    // GET PROVIDER
    // =========================================================

    @GetMapping("/{providerId}")
    public ResponseEntity<ProviderSearchResponse> getProvider(
            @PathVariable UUID providerId
    ) {

        return searchProvidersUseCase
                .getProvider(providerId)
                .map(providerSearchMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(
                        () -> ResponseEntity.notFound().build()
                );
    }
}