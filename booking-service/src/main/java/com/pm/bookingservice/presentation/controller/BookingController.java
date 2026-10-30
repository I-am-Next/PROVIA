package com.pm.bookingservice.presentation.controller;

import com.pm.bookingservice.application.dto.query.BookingFilter;
import com.pm.bookingservice.application.dto.request.CreateBookingRequest;
import com.pm.bookingservice.application.dto.response.*;
import com.pm.bookingservice.application.service.BookingService;
import com.pm.bookingservice.application.dto.response.quote.BookingQuoteResponse;
import com.pm.bookingservice.application.service.QuoteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService service;
    private final QuoteService quoteService;


    public BookingController(

            BookingService service,

            QuoteService quoteService

    ) {

        this.service = service;

        this.quoteService = quoteService;

    }


    @PostMapping
    public BookingResponse createBooking(
            @Valid
            @RequestBody
            CreateBookingRequest request

    ) {
        return service.createBooking(
                request
        );

    }

    @GetMapping("/{id}")
    public BookingResponse getBooking(

            @PathVariable
            UUID id

    ) {

        return service.getBooking(
                id
        );

    }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(

            @PathVariable
            UUID id

    ) {


        return service.cancelBooking(

                id

        );

    }

    @GetMapping("/{id}/quote")
    public BookingQuoteResponse getQuote(

            @PathVariable
            UUID id

    ) {

        return quoteService.getQuote(

                id

        );

    }

    @PostMapping("/{id}/quote/accept")
    public BookingQuoteResponse acceptQuote(

            @PathVariable
            UUID id

    ) {

        return quoteService.acceptQuote(

                id

        );

    }

    @PostMapping("/{id}/quote/reject")
    public BookingQuoteResponse rejectQuote(

            @PathVariable
            UUID id

    ) {

        return quoteService.rejectQuote(

                id

        );

    }

    @PostMapping("/{id}/request-material-release")
    public MaterialReleaseResponse requestMaterialRelease(

            @PathVariable
            UUID id

    ) {

        return service.requestMaterialsRelease(id);

    }

    @PostMapping("/{id}/confirm-completion")
    public CompletionConfirmationResponse confirmCompletion(

            @PathVariable
            UUID id

    ) {

        return service.confirmCompletion(

                id

        );

    }

    @GetMapping("/me")
    public Page<BookingResponse> myBookings(

            @RequestParam(defaultValue = "ALL")
            BookingFilter filter,

            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable

    ) {

        return service.listClientBookings(
                filter,
                pageable
        );

    }

    @GetMapping("/me/summary")
    public BookingSummaryResponse bookingSummary() {

        return service.getClientBookingSummary();

    }

    @GetMapping("/me/upcoming")
    public List<BookingResponse> upcomingBookings() {

        return service.listUpcomingClientBookings();

    }

    @GetMapping("/me/recent")
    public List<BookingResponse> recentBookings() {

        return service.listRecentClientBookings();

    }

    @GetMapping("/{id}/status-history")
    public List<BookingStatusHistoryResponse> getStatusHistory(

            @PathVariable
            UUID id

    ) {

        return service.getBookingStatusHistory(id);

    }

    @GetMapping("/{id}/rating-context")
    public BookingRatingContextResponse getRatingContext(

            @PathVariable
            UUID id

    ) {

        return service.getRatingContext(id);

    }

}