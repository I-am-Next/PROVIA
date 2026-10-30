package com.pm.searchservice.domain.ports.in;

import java.util.List;

public interface AutocompleteUseCase {

    List<String> autocomplete(String query);
}