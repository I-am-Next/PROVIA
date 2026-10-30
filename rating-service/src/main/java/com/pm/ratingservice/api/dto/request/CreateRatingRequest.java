package com.pm.ratingservice.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateRatingRequest(

        @NotNull(message = "bookingId is required")
        UUID bookingId,

        @Min(value = 1, message = "score must be at least 1")
        @Max(value = 5, message = "score must not exceed 5")
        int score,

        @Size(
                max = 1000,
                message = "comment must not exceed 1000 characters"
        )
        String comment
) {
}
