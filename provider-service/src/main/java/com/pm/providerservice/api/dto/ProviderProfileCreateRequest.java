package com.pm.providerservice.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ProviderProfileCreateRequest(

        @NotBlank
        String businessName,

        @NotBlank
        String category

) {
}