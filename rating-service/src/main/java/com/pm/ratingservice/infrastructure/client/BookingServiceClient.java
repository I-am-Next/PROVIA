package com.pm.ratingservice.infrastructure.client;

import com.pm.ratingservice.api.dto.response.BookingRatingContextResponse;
import com.pm.ratingservice.application.port.BookingGateway;
import com.pm.ratingservice.infrastructure.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "booking-service",
        url = "${booking-service.url}",
        configuration = FeignConfig.class
)
public interface BookingServiceClient
        extends BookingGateway {

    @Override
    @GetMapping("/bookings/{bookingId}/rating-context")
    BookingRatingContextResponse getRatingContext(
            @PathVariable("bookingId") UUID bookingId
    );
}