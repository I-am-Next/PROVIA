package com.pm.bookingservice.application.service;

import com.pm.bookingservice.application.dto.response.internal.InternalBookingResponse;

import java.util.UUID;

public interface InternalBookingService {

    InternalBookingResponse getBooking(UUID bookingId);

}