package com.pm.bookingservice.application.service;

import com.pm.bookingservice.application.dto.response.internal.InternalBookingResponse;
import com.pm.bookingservice.application.mapper.InternalBookingMapper;
import com.pm.bookingservice.domain.exception.BookingNotFoundException;
import com.pm.bookingservice.domain.model.Booking;
import com.pm.bookingservice.domain.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class InternalBookingServiceImpl implements InternalBookingService {

    private final BookingRepository bookingRepository;
    private final InternalBookingMapper mapper;

    public InternalBookingServiceImpl(
            BookingRepository bookingRepository,
            InternalBookingMapper mapper
    ) {
        this.bookingRepository = bookingRepository;
        this.mapper = mapper;
    }

    @Override
    public InternalBookingResponse getBooking(UUID bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        return mapper.toResponse(booking);

    }

}