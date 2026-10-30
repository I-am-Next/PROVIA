package com.pm.bookingservice.application.service;

import com.pm.bookingservice.application.dto.query.BookingFilter;
import com.pm.bookingservice.application.dto.request.CreateBookingRequest;
import com.pm.bookingservice.application.dto.request.UpdateBookingRequest;
import com.pm.bookingservice.application.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;


public interface BookingService {

    BookingResponse createBooking(
            CreateBookingRequest request
    );


    BookingResponse getBooking(
            UUID bookingId
    );


    MaterialReleaseResponse requestMaterialsRelease(
            UUID bookingId
    );

    Page<BookingResponse> listClientBookings(
            BookingFilter filter,
            Pageable pageable
    );


    Page<BookingResponse> listProviderJobs(
            Pageable pageable
    );


    Page<BookingResponse> listAvailableJobs(
            Pageable pageable
    );


    BookingResponse acceptJob(
            UUID bookingId
    );


    BookingResponse rejectJob(
            UUID bookingId
    );


    BookingResponse startJob(
            UUID bookingId
    );


    BookingResponse completeJob(
            UUID bookingId
    );


    BookingResponse cancelBooking(
            UUID bookingId
    );

    BookingResponse updateBooking(

            UUID bookingId,

            UpdateBookingRequest request

    );

    CompletionConfirmationResponse confirmCompletion(
            UUID bookingId
    );

    BookingSummaryResponse getClientBookingSummary();

    List<BookingResponse> listUpcomingClientBookings();

    List<BookingResponse> listRecentClientBookings();
    List<BookingStatusHistoryResponse> getBookingStatusHistory(
            UUID bookingId
    );

    BookingRatingContextResponse getRatingContext(
            UUID bookingId
    );

}
