package com.pm.providerservice.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProviderProfileUpdateRequest(

        @NotBlank
        String businessName,

        @NotBlank
        String category,

        @Size(max = 500)
        String description,

        @Size(max = 20)
        String phone

) {
}