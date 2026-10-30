package com.pm.searchservice.api.controller;

import com.pm.searchservice.api.dto.AutocompleteResponse;
import com.pm.searchservice.api.mapper.ProviderSearchMapper;
import com.pm.searchservice.domain.ports.in.AutocompleteUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
public class AutocompleteController {

    private final AutocompleteUseCase autocompleteUseCase;
    private final ProviderSearchMapper providerSearchMapper;

    public AutocompleteController(
            AutocompleteUseCase autocompleteUseCase,
            ProviderSearchMapper providerSearchMapper
    ) {
        this.autocompleteUseCase =
                autocompleteUseCase;

        this.providerSearchMapper =
                providerSearchMapper;
    }

    @GetMapping("/autocomplete")
    public AutocompleteResponse autocomplete(
            @RequestParam String query
    ) {

        return providerSearchMapper.toAutocompleteResponse(
                autocompleteUseCase.autocomplete(query)
        );
    }
}