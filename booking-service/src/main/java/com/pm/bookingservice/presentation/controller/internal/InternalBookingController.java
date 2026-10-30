package com.pm.bookingservice.presentation.controller.internal;

import com.pm.bookingservice.application.dto.response.internal.InternalBookingResponse;
import com.pm.bookingservice.application.service.InternalBookingService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/bookings")
public class InternalBookingController {

    private final InternalBookingService internalBookingService;

    public InternalBookingController(
            InternalBookingService internalBookingService
    ) {
        this.internalBookingService = internalBookingService;
    }

    @GetMapping("/{bookingId}")
    public InternalBookingResponse getBooking(
            @PathVariable UUID bookingId
    ) {
        return internalBookingService.getBooking(bookingId);
    }

}