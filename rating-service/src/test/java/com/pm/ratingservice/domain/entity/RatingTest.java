package com.pm.ratingservice.domain.entity;

import com.pm.ratingservice.domain.enums.ReviewerRole;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RatingTest {

    private final UUID bookingId = UUID.randomUUID();
    private final UUID reviewerUserId = UUID.randomUUID();
    private final UUID revieweeUserId = UUID.randomUUID();

    @Test
    void shouldAcceptMinimumScoreOfOne() {
        Rating rating = assertDoesNotThrow(() ->
                Rating.create(
                        bookingId,
                        reviewerUserId,
                        ReviewerRole.CUSTOMER,
                        revieweeUserId,
                        ReviewerRole.PROVIDER,
                        1,
                        "Good work"
                )
        );

        assertEquals(1, rating.getScore());
    }

    @Test
    void shouldAcceptMaximumScoreOfFive() {
        Rating rating = assertDoesNotThrow(() ->
                Rating.create(
                        bookingId,
                        reviewerUserId,
                        ReviewerRole.CUSTOMER,
                        revieweeUserId,
                        ReviewerRole.PROVIDER,
                        5,
                        "Excellent work"
                )
        );

        assertEquals(5, rating.getScore());
    }

    @Test
    void shouldRejectScoreBelowOne() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Rating.create(
                        bookingId,
                        reviewerUserId,
                        ReviewerRole.CUSTOMER,
                        revieweeUserId,
                        ReviewerRole.PROVIDER,
                        0,
                        "Invalid rating"
                )
        );

        assertEquals(
                "Rating score must be between 1 and 5",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectScoreAboveFive() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Rating.create(
                        bookingId,
                        reviewerUserId,
                        ReviewerRole.CUSTOMER,
                        revieweeUserId,
                        ReviewerRole.PROVIDER,
                        6,
                        "Invalid rating"
                )
        );

        assertEquals(
                "Rating score must be between 1 and 5",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNegativeScore() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Rating.create(
                        bookingId,
                        reviewerUserId,
                        ReviewerRole.CUSTOMER,
                        revieweeUserId,
                        ReviewerRole.PROVIDER,
                        -1,
                        "Invalid rating"
                )
        );
    }

    @Test
    void shouldRejectInvalidScoreWhenUpdating() {
        Rating rating = Rating.create(
                bookingId,
                reviewerUserId,
                ReviewerRole.CUSTOMER,
                revieweeUserId,
                ReviewerRole.PROVIDER,
                4,
                "Good work"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> rating.update(6, "Invalid update")
        );

        assertEquals(4, rating.getScore());
        assertEquals("Good work", rating.getComment());
    }
}