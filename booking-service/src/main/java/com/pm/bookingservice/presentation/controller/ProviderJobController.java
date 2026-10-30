package com.pm.bookingservice.presentation.controller;

import com.pm.bookingservice.application.dto.response.BookingResponse;
import com.pm.bookingservice.application.service.BookingService;
import com.pm.bookingservice.application.dto.request.quote.CreateQuoteRequest;
import com.pm.bookingservice.application.dto.response.quote.BookingQuoteResponse;
import com.pm.bookingservice.application.service.QuoteService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/bookings/provider")
public class ProviderJobController {

    private final BookingService service;
    private final QuoteService quoteService;

    public ProviderJobController(

            BookingService service,

            QuoteService quoteService

    ) {

        this.service = service;

        this.quoteService = quoteService;

    }

    /**
     * Provider's accepted/requested jobs
     */
    @GetMapping
    public Page<BookingResponse> jobs(
            Pageable pageable
    ) {

        return service.listProviderJobs(
                pageable
        );

    }

    /**
     * Available jobs for providers
     */
    @GetMapping("/available")
    public Page<BookingResponse> availableJobs(
            Pageable pageable
    ) {

        return service.listAvailableJobs(
                pageable
        );

    }


    /**
     * Get booking details
     */
    @GetMapping("/{id}")
    public BookingResponse booking(

            @PathVariable
            UUID id

    ) {

        return service.getBooking(
                id
        );

    }


    /**
     * Accept booking
     */
    @PostMapping("/{id}/accept")
    public BookingResponse accept(

            @PathVariable
            UUID id

    ) {

        return service.acceptJob(
                id
        );

    }


    /**
     * Reject booking
     */
    @PostMapping("/{id}/reject")
    public BookingResponse reject(

            @PathVariable
            UUID id

    ) {

        return service.rejectJob(
                id
        );

    }


    /**
     * Start work
     */
    @PostMapping("/{id}/start")
    public BookingResponse start(

            @PathVariable
            UUID id

    ) {

        return service.startJob(
                id
        );

    }


    /**
     * Complete work
     */
    @PostMapping("/{id}/complete")
    public BookingResponse complete(

            @PathVariable
            UUID id

    ) {

        return service.completeJob(
                id
        );

    }

    @PostMapping("/{id}/quote")
    public BookingQuoteResponse createQuote(

            @PathVariable
            UUID id,

            @Valid
            @RequestBody
            CreateQuoteRequest request

    ) {


        return quoteService.createQuote(

                id,

                request

        );

    }


}