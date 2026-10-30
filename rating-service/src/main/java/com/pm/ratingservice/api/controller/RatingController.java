package com.pm.ratingservice.api.controller;

import com.pm.ratingservice.api.dto.request.CreateRatingRequest;
import com.pm.ratingservice.api.dto.request.UpdateRatingRequest;
import com.pm.ratingservice.api.dto.response.ProviderRatingSummaryResponse;
import com.pm.ratingservice.api.dto.response.ProviderReviewsResponse;
import com.pm.ratingservice.api.dto.response.RatingResponse;
import com.pm.ratingservice.application.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(
            RatingService ratingService
    ) {
        this.ratingService = ratingService;
    }

    /**
     * Create a rating for a completed booking.
     */
    @PostMapping
    public ResponseEntity<RatingResponse> createRating(
            @Valid @RequestBody CreateRatingRequest request
    ) {

        RatingResponse response =
                ratingService.createRating(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get a rating by ID.
     */
    @GetMapping("/{ratingId}")
    public ResponseEntity<RatingResponse> getRating(
            @PathVariable UUID ratingId
    ) {

        return ResponseEntity.ok(
                ratingService.getRating(ratingId)
        );
    }

    /**
     * Update an existing rating.
     */
    @PutMapping("/{ratingId}")
    public ResponseEntity<RatingResponse> updateRating(
            @PathVariable UUID ratingId,
            @Valid @RequestBody UpdateRatingRequest request
    ) {

        return ResponseEntity.ok(
                ratingService.updateRating(
                        ratingId,
                        request
                )
        );
    }

    /**
     * Delete an existing rating.
     */
    @DeleteMapping("/{ratingId}")
    public ResponseEntity<Void> deleteRating(
            @PathVariable UUID ratingId
    ) {

        ratingService.deleteRating(ratingId);

        return ResponseEntity.noContent().build();
    }

    /**
     * Get the authenticated user's rating for a booking.
     */
    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<RatingResponse> getRatingByBookingId(
            @PathVariable UUID bookingId
    ) {

        return ResponseEntity.ok(
                ratingService.getRatingByBookingId(
                        bookingId
                )
        );
    }

    /**
     * Get provider's rating summary.
     */
    @GetMapping("/providers/{providerId}/summary")
    public ResponseEntity<ProviderRatingSummaryResponse>
    getProviderRatingSummary(
            @PathVariable UUID providerId
    ) {

        return ResponseEntity.ok(
                ratingService.getProviderRatingSummary(
                        providerId
                )
        );
    }

    /**
     * Get provider reviews.
     */
    @GetMapping("/providers/{providerId}/reviews")
    public ResponseEntity<ProviderReviewsResponse>
    getProviderReviews(
            @PathVariable UUID providerId,
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                ratingService.getProviderReviews(
                        providerId,
                        pageable
                )
        );
    }
}