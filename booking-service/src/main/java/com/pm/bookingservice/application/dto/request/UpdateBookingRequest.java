package com.pm.bookingservice.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateBookingRequest(

        @Valid
        @NotNull(message = "Address is required")
        BookingAddressRequest address,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Size(max = 1000, message = "Notes must not exceed 1000 characters")
        String notes,

        @NotNull(message = "Scheduled start is required")
        @Future(message = "Scheduled start must be in the future")
        LocalDateTime scheduledStart,

        @NotNull(message = "Scheduled end is required")
        @Future(message = "Scheduled end must be in the future")
        LocalDateTime scheduledEnd

) {

        @AssertTrue(message = "Scheduled end must be after scheduled start")
        public boolean isScheduleValid() {

                if (scheduledStart == null || scheduledEnd == null) {
                        return true;
                }

                return scheduledEnd.isAfter(scheduledStart);
        }
}