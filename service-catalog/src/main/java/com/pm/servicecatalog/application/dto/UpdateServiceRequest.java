package com.pm.servicecatalog.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateServiceRequest(

        @NotBlank(message = "Service name is required")
        String name,

        String description,

        @NotNull(message = "Category ID is required")
        UUID categoryId

) {
}