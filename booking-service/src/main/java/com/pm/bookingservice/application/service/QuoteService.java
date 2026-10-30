package com.pm.bookingservice.application.service;

import com.pm.bookingservice.application.dto.request.quote.CreateQuoteRequest;
import com.pm.bookingservice.application.dto.response.quote.BookingQuoteResponse;

import java.util.UUID;

public interface QuoteService {


    BookingQuoteResponse createQuote(

            UUID bookingId,

            CreateQuoteRequest request

    );


    BookingQuoteResponse getQuote(

            UUID bookingId

    );


    BookingQuoteResponse acceptQuote(

            UUID bookingId

    );


    BookingQuoteResponse rejectQuote(

            UUID bookingId

    );


}