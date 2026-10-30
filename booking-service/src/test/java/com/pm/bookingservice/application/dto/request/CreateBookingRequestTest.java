package com.pm.bookingservice.application.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CreateBookingRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void shouldAcceptValidCreateBookingRequest() {

        CreateBookingRequest request = validRequest();

        Set<ConstraintViolation<CreateBookingRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectMissingProviderServiceId() {

        CreateBookingRequest request = new CreateBookingRequest(
                null,
                validAddress(),
                "Repair kitchen sink",
                "Customer notes",
                futureStart(),
                futureEnd()
        );

        Set<ConstraintViolation<CreateBookingRequest>> violations =
                validator.validate(request);

        assertHasViolation(
                violations,
                "providerServiceId",
                "Provider service is required"
        );
    }

    @Test
    void shouldRejectMissingAddress() {

        CreateBookingRequest request = new CreateBookingRequest(
                UUID.randomUUID(),
                null,
                "Repair kitchen sink",
                null,
                futureStart(),
                futureEnd()
        );

        Set<ConstraintViolation<CreateBookingRequest>> violations =
                validator.validate(request);

        assertHasViolation(
                violations,
                "address",
                "Address is required"
        );
    }

    @Test
    void shouldRejectInvalidNestedAddress() {

        BookingAddressRequest invalidAddress =
                new BookingAddressRequest(
                        "",
                        null,
                        "",
                        null,
                        null,
                        ""
                );

        CreateBookingRequest request = new CreateBookingRequest(
                UUID.randomUUID(),
                invalidAddress,
                "Repair kitchen sink",
                null,
                futureStart(),
                futureEnd()
        );

        Set<ConstraintViolation<CreateBookingRequest>> violations =
                validator.validate(request);

        assertHasViolation(
                violations,
                "address.addressLine1",
                "Address line 1 is required"
        );

        assertHasViolation(
                violations,
                "address.city",
                "City is required"
        );

        assertHasViolation(
                violations,
                "address.country",
                "Country is required"
        );
    }

    @Test
    void shouldRejectMissingScheduledStart() {

        CreateBookingRequest request = new CreateBookingRequest(
                UUID.randomUUID(),
                validAddress(),
                null,
                null,
                null,
                futureEnd()
        );

        Set<ConstraintViolation<CreateBookingRequest>> violations =
                validator.validate(request);

        assertHasViolation(
                violations,
                "scheduledStart",
                "Scheduled start is required"
        );
    }

    @Test
    void shouldRejectPastScheduledStart() {

        CreateBookingRequest request = new CreateBookingRequest(
                UUID.randomUUID(),
                validAddress(),
                null,
                null,
                LocalDateTime.now().minusHours(1),
                futureEnd()
        );

        Set<ConstraintViolation<CreateBookingRequest>> violations =
                validator.validate(request);

        assertHasViolation(
                violations,
                "scheduledStart",
                "Scheduled start must be in the future"
        );
    }

    @Test
    void shouldRejectMissingScheduledEnd() {

        CreateBookingRequest request = new CreateBookingRequest(
                UUID.randomUUID(),
                validAddress(),
                null,
                null,
                futureStart(),
                null
        );

        Set<ConstraintViolation<CreateBookingRequest>> violations =
                validator.validate(request);

        assertHasViolation(
                violations,
                "scheduledEnd",
                "Scheduled end is required"
        );
    }

    @Test
    void shouldRejectPastScheduledEnd() {

        CreateBookingRequest request = new CreateBookingRequest(
                UUID.randomUUID(),
                validAddress(),
                null,
                null,
                futureStart(),
                LocalDateTime.now().minusHours(1)
        );

        Set<ConstraintViolation<CreateBookingRequest>> violations =
                validator.validate(request);

        assertHasViolation(
                violations,
                "scheduledEnd",
                "Scheduled end must be in the future"
        );
    }

    @Test
    void shouldRejectWhenScheduledEndIsBeforeScheduledStart() {

        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = start.minusHours(1);

        CreateBookingRequest request = new CreateBookingRequest(
                UUID.randomUUID(),
                validAddress(),
                null,
                null,
                start,
                end
        );

        Set<ConstraintViolation<CreateBookingRequest>> violations =
                validator.validate(request);

        assertHasViolation(
                violations,
                "scheduleValid",
                "Scheduled end must be after scheduled start"
        );
    }

    @Test
    void shouldRejectWhenScheduledEndEqualsScheduledStart() {

        LocalDateTime scheduledTime =
                LocalDateTime.now().plusDays(2);

        CreateBookingRequest request = new CreateBookingRequest(
                UUID.randomUUID(),
                validAddress(),
                null,
                null,
                scheduledTime,
                scheduledTime
        );

        Set<ConstraintViolation<CreateBookingRequest>> violations =
                validator.validate(request);

        assertHasViolation(
                violations,
                "scheduleValid",
                "Scheduled end must be after scheduled start"
        );
    }

    @Test
    void shouldAcceptNullDescription() {

        CreateBookingRequest request = new CreateBookingRequest(
                UUID.randomUUID(),
                validAddress(),
                null,
                null,
                futureStart(),
                futureEnd()
        );

        Set<ConstraintViolation<CreateBookingRequest>> violations =
                validator.validate(request);

        assertFalse(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("description"))
        );
    }

    @Test
    void shouldRejectDescriptionLongerThan1000Characters() {

        String description = "a".repeat(1001);

        CreateBookingRequest request = new CreateBookingRequest(
                UUID.randomUUID(),
                validAddress(),
                description,
                null,
                futureStart(),
                futureEnd()
        );

        Set<ConstraintViolation<CreateBookingRequest>> violations =
                validator.validate(request);

        assertHasViolation(
                violations,
                "description",
                "Description must not exceed 1000 characters"
        );
    }

    @Test
    void shouldAcceptDescriptionWithExactly1000Characters() {

        String description = "a".repeat(1000);

        CreateBookingRequest request = new CreateBookingRequest(
                UUID.randomUUID(),
                validAddress(),
                description,
                null,
                futureStart(),
                futureEnd()
        );

        Set<ConstraintViolation<CreateBookingRequest>> violations =
                validator.validate(request);

        assertFalse(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("description"))
        );
    }

    @Test
    void shouldRejectNotesLongerThan1000Characters() {

        String notes = "a".repeat(1001);

        CreateBookingRequest request = new CreateBookingRequest(
                UUID.randomUUID(),
                validAddress(),
                null,
                notes,
                futureStart(),
                futureEnd()
        );

        Set<ConstraintViolation<CreateBookingRequest>> violations =
                validator.validate(request);

        assertHasViolation(
                violations,
                "notes",
                "Notes must not exceed 1000 characters"
        );
    }

    @Test
    void shouldAcceptNotesWithExactly1000Characters() {

        String notes = "a".repeat(1000);

        CreateBookingRequest request = new CreateBookingRequest(
                UUID.randomUUID(),
                validAddress(),
                null,
                notes,
                futureStart(),
                futureEnd()
        );

        Set<ConstraintViolation<CreateBookingRequest>> violations =
                validator.validate(request);

        assertFalse(
                violations.stream()
                        .anyMatch(v ->
                                v.getPropertyPath()
                                        .toString()
                                        .equals("notes"))
        );
    }

    @Test
    void shouldAcceptScheduleWhenBothDatesAreFutureAndEndIsAfterStart() {

        LocalDateTime start =
                LocalDateTime.now().plusDays(2);

        LocalDateTime end =
                start.plusHours(2);

        CreateBookingRequest request = new CreateBookingRequest(
                UUID.randomUUID(),
                validAddress(),
                null,
                null,
                start,
                end
        );

        Set<ConstraintViolation<CreateBookingRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------

    private static CreateBookingRequest validRequest() {

        LocalDateTime start =
                LocalDateTime.now().plusDays(2);

        LocalDateTime end =
                start.plusHours(2);

        return new CreateBookingRequest(
                UUID.randomUUID(),
                validAddress(),
                "Repair kitchen sink",
                "Customer notes",
                start,
                end
        );
    }

    private static BookingAddressRequest validAddress() {

        return new BookingAddressRequest(
                "12 Test Street",
                null,
                "Maiduguri",
                "Borno",
                "600001",
                "Nigeria"
        );
    }

    private static LocalDateTime futureStart() {

        return LocalDateTime.now().plusDays(2);
    }

    private static LocalDateTime futureEnd() {

        return LocalDateTime.now().plusDays(2).plusHours(2);
    }

    private static void assertHasViolation(
            Set<? extends ConstraintViolation<?>> violations,
            String property,
            String message
    ) {

        assertTrue(
                violations.stream().anyMatch(
                        violation ->
                                violation.getPropertyPath()
                                        .toString()
                                        .equals(property)
                                        && violation.getMessage()
                                        .equals(message)
                ),
                () -> "Expected violation [" +
                        property +
                        "] with message [" +
                        message +
                        "] but got: " +
                        violations
        );
    }
}