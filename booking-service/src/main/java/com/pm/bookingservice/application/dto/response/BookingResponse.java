package com.pm.bookingservice.application.dto.response;

import com.pm.bookingservice.domain.model.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponse(
      UUID id,

      UUID userId,

      UUID providerUserId,

      UUID providerServiceId,

      String serviceName,

      String description,

      BigDecimal price,

      BookingAddressResponse address,

      LocalDateTime scheduledStart,

      LocalDateTime scheduledEnd,

      BookingStatus status,

      String notes,

      Instant createdAt) { }
