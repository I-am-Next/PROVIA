package com.pm.servicecatalog.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateServiceRequest(
        @NotBlank String name,
        String description,
        @NotNull UUID categoryId) { }
