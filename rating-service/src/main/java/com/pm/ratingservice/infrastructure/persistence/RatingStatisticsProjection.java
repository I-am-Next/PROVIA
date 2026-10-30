package com.pm.ratingservice.infrastructure.persistence;

public interface RatingStatisticsProjection {

    Double getAverageRating();

    Long getTotalRatings();
}