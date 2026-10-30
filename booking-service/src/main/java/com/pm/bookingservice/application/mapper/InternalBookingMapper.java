package com.pm.bookingservice.application.mapper;

import com.pm.bookingservice.application.dto.response.internal.InternalBookingResponse;
import com.pm.bookingservice.domain.model.Booking;
import org.springframework.stereotype.Component;

@Component
public class InternalBookingMapper {

    public InternalBookingResponse toResponse(Booking booking) {

        return new InternalBookingResponse(

                booking.getId(),

                booking.getUserId(),

                booking.getProviderUserId(),

                booking.getPrice(),

                booking.getStatus()

        );

    }

}