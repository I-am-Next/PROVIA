package com.pm.servicecatalog.application.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCategoryRequest(

        @NotBlank(message = "Category name is required")
        String name,

        String description

) {
}