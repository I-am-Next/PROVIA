package com.pm.searchservice.api.controller;

import com.pm.searchservice.api.dto.CatalogAutocompleteResponse;
import com.pm.searchservice.api.mapper.CatalogAutocompleteMapper;
import com.pm.searchservice.domain.ports.in.CatalogAutocompleteUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search/catalog")
public class CatalogAutocompleteController {

    private final CatalogAutocompleteUseCase catalogAutocompleteUseCase;
    private final CatalogAutocompleteMapper catalogAutocompleteMapper;

    public CatalogAutocompleteController(
            CatalogAutocompleteUseCase catalogAutocompleteUseCase,
            CatalogAutocompleteMapper catalogAutocompleteMapper
    ) {
        this.catalogAutocompleteUseCase = catalogAutocompleteUseCase;
        this.catalogAutocompleteMapper = catalogAutocompleteMapper;
    }

    @GetMapping("/autocomplete")
    public CatalogAutocompleteResponse autocomplete(
            @RequestParam String query
    ) {

        return catalogAutocompleteMapper.toResponse(
                catalogAutocompleteUseCase.autocomplete(query)
        );
    }
}