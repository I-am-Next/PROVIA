package com.pm.paymentservicenew.infrastructure.client;

import com.pm.paymentservicenew.infrastructure.config.FeignConfig;
import com.pm.paymentservicenew.integration.dto.InternalBookingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "booking-service",
        url = "${booking-service.url}",
        configuration = FeignConfig.class
)
public interface BookingClient {

    @GetMapping("/internal/bookings/{bookingId}")
    InternalBookingResponse getBooking(
            @PathVariable UUID bookingId
    );

}