package com.pm.bookingservice.domain.repository;

import com.pm.bookingservice.domain.model.BookingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingStatusHistoryRepository
        extends JpaRepository<BookingStatusHistory, UUID> {

    List<BookingStatusHistory> findByBookingIdOrderByChangedAtAsc(
            UUID bookingId
    );
}