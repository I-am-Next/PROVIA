package com.pm.searchservice.api.dto;

import java.util.List;

public record AutocompleteResponse(
        List<String> suggestions
) {
}