package com.pm.bookingservice.application.mapper;

import com.pm.bookingservice.application.dto.response.quote.BookingQuoteResponse;
import com.pm.bookingservice.domain.model.quote.BookingQuote;

public class BookingQuoteMapper {

    private BookingQuoteMapper() {

    }

    public static BookingQuoteResponse toResponse(
            BookingQuote quote
    ) {

        return new BookingQuoteResponse(

                quote.getId(),
                quote.getBookingId(),
                quote.getProviderId(),
                quote.getLabourAmount(),
                quote.getMaterialsAmount(),
                quote.getServiceFee(),
                quote.getTotalAmount(),
                quote.getDescription(),
                quote.getEstimatedDurationDays(),
                quote.getStatus(),
                quote.getExpiresAt(),
                quote.getCreatedAt()

        );

    }

}