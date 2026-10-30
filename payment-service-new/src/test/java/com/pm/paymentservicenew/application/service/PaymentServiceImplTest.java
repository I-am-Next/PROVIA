package com.pm.paymentservicenew.application.service;

import com.pm.paymentservicenew.application.dto.request.CreatePaymentRequest;
import com.pm.paymentservicenew.application.event.DomainEventPublisher;
import com.pm.paymentservicenew.application.factory.EscrowEventFactory;
import com.pm.paymentservicenew.application.factory.PaymentEventFactory;
import com.pm.paymentservicenew.application.mapper.PaymentMapper;
import com.pm.paymentservicenew.domain.model.escrow.Escrow;
import com.pm.paymentservicenew.domain.model.payment.Payment;
import com.pm.paymentservicenew.domain.repository.EscrowRepository;
import com.pm.paymentservicenew.domain.repository.PaymentRepository;
import com.pm.paymentservicenew.infrastructure.client.BookingClient;
import com.pm.paymentservicenew.integration.dto.BookingStatus;
import com.pm.paymentservicenew.integration.dto.InternalBookingResponse;
import com.pm.paymentservicenew.integration.paystack.dto.VerifyTransactionResponse;
import com.pm.paymentservicenew.integration.paystack.service.PaymentGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @Mock
    private BookingClient bookingClient;

    @Mock
    private EscrowRepository escrowRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Mock
    private PaymentEventFactory paymentEventFactory;

    @Mock
    private EscrowEventFactory escrowEventFactory;

    private PaymentServiceImpl paymentService;

    private UUID paymentId;
    private UUID bookingId;
    private UUID providerId;

    private Payment payment;

    @BeforeEach
    void setUp() {

        paymentService = new PaymentServiceImpl(
                paymentRepository,
                paymentMapper,
                paymentGatewayService,
                paymentEventFactory,
                bookingClient,
                escrowRepository,
                domainEventPublisher,
                escrowEventFactory
        );

        paymentId = UUID.randomUUID();
        bookingId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        providerId = UUID.randomUUID();

        payment = Payment.create(
                bookingId,
                customerId,
                providerId,
                new BigDecimal("50000.00")
        );

        /*
         * Payment.create() generates its own ID.
         * We need the verification reference to match
         * the actual Payment ID.
         */
        setPaymentId(payment, paymentId);
    }

    @Test
    void shouldRejectVerificationWhenGatewayAmountDoesNotMatchPaymentAmount() {

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        VerifyTransactionResponse verification =
                new VerifyTransactionResponse(
                        50000L, // ₦500.00 in kobo
                        "NGN",
                        "success",
                        paymentId.toString(),
                        Instant.now().toString(),
                        "Approved"
                );

        when(paymentGatewayService.verifyPayment(paymentId.toString()))
                .thenReturn(verification);

        assertThrows(
                RuntimeException.class,
                () -> paymentService.verifyPayment(
                        paymentId.toString()
                )
        );

        verify(paymentGatewayService)
                .verifyPayment(paymentId.toString());

        verify(paymentRepository, never())
                .save(argThat(Payment::isCompleted));

        verify(escrowRepository, never())
                .save(any(Escrow.class));

        verifyNoInteractions(domainEventPublisher);
    }

    @Test
    void shouldRejectVerificationWhenGatewayCurrencyDoesNotMatchPaymentCurrency() {

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        VerifyTransactionResponse verification =
                new VerifyTransactionResponse(
                        5_000_000L,
                        "USD",
                        "success",
                        paymentId.toString(),
                        Instant.now().toString(),
                        "Approved"
                );

        when(paymentGatewayService.verifyPayment(paymentId.toString()))
                .thenReturn(verification);

        assertThrows(
                RuntimeException.class,
                () -> paymentService.verifyPayment(
                        paymentId.toString()
                )
        );

        verify(paymentGatewayService)
                .verifyPayment(paymentId.toString());

        verify(paymentRepository, never())
                .save(argThat(Payment::isCompleted));

        verify(escrowRepository, never())
                .save(any(Escrow.class));

        verifyNoInteractions(domainEventPublisher);
    }

    @Test
    void shouldRejectVerificationWhenGatewayReferenceDoesNotMatchPaymentReference() {

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        VerifyTransactionResponse verification =
                new VerifyTransactionResponse(
                        5_000_000L,
                        "NGN",
                        "success",
                        UUID.randomUUID().toString(),
                        Instant.now().toString(),
                        "Approved"
                );

        when(paymentGatewayService.verifyPayment(paymentId.toString()))
                .thenReturn(verification);

        assertThrows(
                RuntimeException.class,
                () -> paymentService.verifyPayment(
                        paymentId.toString()
                )
        );

        verify(paymentGatewayService)
                .verifyPayment(paymentId.toString());

        verify(paymentRepository, never())
                .save(argThat(Payment::isCompleted));

        verify(escrowRepository, never())
                .save(any(Escrow.class));

        verifyNoInteractions(domainEventPublisher);
    }

    @Test
    void shouldRejectVerificationWhenGatewayStatusIsNotSuccessful() {

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        VerifyTransactionResponse verification =
                new VerifyTransactionResponse(
                        5_000_000L,
                        "NGN",
                        "failed",
                        paymentId.toString(),
                        Instant.now().toString(),
                        "Declined"
                );

        when(paymentGatewayService.verifyPayment(paymentId.toString()))
                .thenReturn(verification);

        assertThrows(
                RuntimeException.class,
                () -> paymentService.verifyPayment(
                        paymentId.toString()
                )
        );

        verify(paymentGatewayService)
                .verifyPayment(paymentId.toString());

        verify(paymentRepository, never())
                .save(argThat(Payment::isCompleted));

        verify(escrowRepository, never())
                .save(any(Escrow.class));

        verifyNoInteractions(domainEventPublisher);
    }

    @Test
    void shouldCompletePaymentCreateEscrowAndPublishEventsWhenGatewayVerificationSucceeds() {

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        VerifyTransactionResponse verification =
                new VerifyTransactionResponse(
                        5_000_000L,
                        "NGN",
                        "success",
                        paymentId.toString(),
                        Instant.now().toString(),
                        "Approved"
                );

        when(paymentGatewayService.verifyPayment(paymentId.toString()))
                .thenReturn(verification);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(escrowRepository.save(any(Escrow.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.verifyPayment(
                paymentId.toString()
        );

        assertTrue(payment.isCompleted());

        assertEquals(
                paymentId.toString(),
                payment.getGatewayReference()
        );

        verify(paymentGatewayService)
                .verifyPayment(paymentId.toString());

        verify(paymentRepository)
                .save(payment);

        ArgumentCaptor<Escrow> escrowCaptor =
                ArgumentCaptor.forClass(Escrow.class);

        verify(escrowRepository)
                .save(escrowCaptor.capture());

        Escrow savedEscrow = escrowCaptor.getValue();

        assertEquals(
                paymentId,
                savedEscrow.getPaymentId()
        );

        assertEquals(
                bookingId,
                savedEscrow.getBookingId()
        );

        assertEquals(
                providerId,
                savedEscrow.getProviderId()
        );

        assertEquals(
                0,
                savedEscrow.getTotalAmount()
                        .compareTo(payment.getAmount())
        );

        verify(paymentEventFactory)
                .paymentCompleted(
                        payment,
                        savedEscrow
                );

        verify(escrowEventFactory)
                .escrowCreated(
                        savedEscrow
                );

        verify(domainEventPublisher, times(2))
                .publish(any());
    }


    @Test
    void shouldCreateEscrowWithCorrectPaymentAndBookingDetailsWhenVerificationSucceeds() {

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        VerifyTransactionResponse verification =
                new VerifyTransactionResponse(
                        5_000_000L,
                        "NGN",
                        "success",
                        paymentId.toString(),
                        Instant.now().toString(),
                        "Approved"
                );

        when(paymentGatewayService.verifyPayment(paymentId.toString()))
                .thenReturn(verification);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(escrowRepository.save(any(Escrow.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.verifyPayment(
                paymentId.toString()
        );

        verify(escrowRepository).save(
                argThat(escrow ->
                        escrow.getPaymentId().equals(paymentId)
                                && escrow.getBookingId().equals(bookingId)
                                && escrow.getProviderId().equals(providerId)
                                && escrow.getTotalAmount().compareTo(
                                payment.getAmount()
                        ) == 0
                )
        );
    }


    @Test
    void shouldSkipVerificationWhenPaymentIsAlreadyCompleted() {

        payment.assignGatewayReference(
                paymentId.toString()
        );

        payment.complete(
                Instant.now()
        );

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        paymentService.verifyPayment(
                paymentId.toString()
        );

        assertTrue(payment.isCompleted());

        verify(paymentRepository)
                .findById(paymentId);

        verify(paymentGatewayService, never())
                .verifyPayment(anyString());

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verify(escrowRepository, never())
                .save(any(Escrow.class));

        verifyNoInteractions(domainEventPublisher);

        verifyNoInteractions(paymentEventFactory);

        verifyNoInteractions(escrowEventFactory);
    }


    @Test
    void shouldThrowWhenPaymentDoesNotExist() {

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> paymentService.verifyPayment(
                        paymentId.toString()
                )
        );

        verify(paymentRepository)
                .findById(paymentId);

        verify(paymentGatewayService, never())
                .verifyPayment(anyString());

        verifyNoInteractions(escrowRepository);
        verifyNoInteractions(domainEventPublisher);
    }


    @Test
    void shouldRejectInvalidPaymentReference() {

        String invalidReference = "not-a-valid-uuid";

        assertThrows(
                RuntimeException.class,
                () -> paymentService.verifyPayment(
                        invalidReference
                )
        );

        verify(paymentRepository, never())
                .findById(any());

        verify(paymentGatewayService, never())
                .verifyPayment(anyString());

        verifyNoInteractions(escrowRepository);
        verifyNoInteractions(domainEventPublisher);
    }



    @Test
    void shouldFailPaymentAndNotCreateEscrowWhenGatewayVerificationFails() {

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        when(paymentGatewayService.verifyPayment(paymentId.toString()))
                .thenThrow(
                        new RuntimeException(
                                "Paystack verification failed"
                        )
                );

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(
                RuntimeException.class,
                () -> paymentService.verifyPayment(
                        paymentId.toString()
                )
        );

        assertTrue(payment.isFailed());

        verify(paymentGatewayService)
                .verifyPayment(paymentId.toString());

        verify(paymentRepository)
                .save(payment);

        verify(escrowRepository, never())
                .save(any(Escrow.class));

        verifyNoInteractions(domainEventPublisher);

        verify(paymentEventFactory, never())
                .paymentCompleted(
                        any(),
                        any()
                );

        verify(escrowEventFactory, never())
                .escrowCreated(any());
    }
    @Test
    void shouldFailPaymentCreationWhenBookingLookupFails() {

        CreatePaymentRequest request =
                new CreatePaymentRequest(
                        bookingId,
                        "customer@example.com"
                );

        UUID customerId = UUID.randomUUID();

        when(bookingClient.getBooking(bookingId))
                .thenThrow(
                        new RuntimeException(
                                "Booking Service unavailable"
                        )
                );

        assertThrows(
                RuntimeException.class,
                () -> paymentService.createPayment(
                        request,
                        customerId
                )
        );

        verify(bookingClient)
                .getBooking(bookingId);

        verify(paymentRepository, never())
                .existsByBookingId(any());

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verify(paymentGatewayService, never())
                .initializePayment(
                        any(),
                        anyString(),
                        any()
                );

        verifyNoInteractions(escrowRepository);
        verifyNoInteractions(domainEventPublisher);
    }

    @Test
    void shouldFailVerificationWhenEscrowPersistenceFails() {

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        VerifyTransactionResponse verification =
                new VerifyTransactionResponse(
                        5_000_000L,
                        "NGN",
                        "success",
                        paymentId.toString(),
                        Instant.now().toString(),
                        "Approved"
                );

        when(paymentGatewayService.verifyPayment(paymentId.toString()))
                .thenReturn(verification);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(escrowRepository.save(any(Escrow.class)))
                .thenThrow(
                        new RuntimeException(
                                "Escrow persistence failed"
                        )
                );

        assertThrows(
                RuntimeException.class,
                () -> paymentService.verifyPayment(
                        paymentId.toString()
                )
        );

        assertTrue(payment.isCompleted());

        verify(paymentGatewayService)
                .verifyPayment(paymentId.toString());

        verify(paymentRepository)
                .save(payment);

        verify(escrowRepository)
                .save(any(Escrow.class));

        verifyNoInteractions(domainEventPublisher);

        verifyNoInteractions(paymentEventFactory);

        verifyNoInteractions(escrowEventFactory);
    }


    @Test
    void shouldFailVerificationWhenPaymentCompletedEventPublicationFails() {

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        VerifyTransactionResponse verification =
                new VerifyTransactionResponse(
                        5_000_000L,
                        "NGN",
                        "success",
                        paymentId.toString(),
                        Instant.now().toString(),
                        "Approved"
                );

        when(paymentGatewayService.verifyPayment(paymentId.toString()))
                .thenReturn(verification);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(escrowRepository.save(any(Escrow.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        RuntimeException eventException =
                new RuntimeException(
                        "PaymentCompleted event publication failed"
                );

        when(paymentEventFactory.paymentCompleted(
                any(Payment.class),
                any(Escrow.class)
        )).thenReturn(null);

        doThrow(eventException)
                .when(domainEventPublisher)
                .publish(any());

        assertThrows(
                RuntimeException.class,
                () -> paymentService.verifyPayment(
                        paymentId.toString()
                )
        );

        assertTrue(payment.isCompleted());

        verify(paymentRepository)
                .save(payment);

        verify(escrowRepository)
                .save(any(Escrow.class));

        verify(paymentEventFactory)
                .paymentCompleted(
                        eq(payment),
                        any(Escrow.class)
                );

        verify(domainEventPublisher)
                .publish(any());

        verify(escrowEventFactory, never())
                .escrowCreated(any());
    }

    @Test
    void shouldFailVerificationWhenEscrowCreatedEventPublicationFails() {

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        VerifyTransactionResponse verification =
                new VerifyTransactionResponse(
                        5_000_000L,
                        "NGN",
                        "success",
                        paymentId.toString(),
                        Instant.now().toString(),
                        "Approved"
                );

        when(paymentGatewayService.verifyPayment(paymentId.toString()))
                .thenReturn(verification);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(escrowRepository.save(any(Escrow.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(paymentEventFactory.paymentCompleted(
                any(Payment.class),
                any(Escrow.class)
        )).thenReturn(null);

        when(escrowEventFactory.escrowCreated(
                any(Escrow.class)
        )).thenReturn(null);

        doNothing()
                .doThrow(
                        new RuntimeException(
                                "EscrowCreated event publication failed"
                        )
                )
                .when(domainEventPublisher)
                .publish(any());

        assertThrows(
                RuntimeException.class,
                () -> paymentService.verifyPayment(
                        paymentId.toString()
                )
        );

        assertTrue(payment.isCompleted());

        verify(paymentRepository)
                .save(payment);

        verify(escrowRepository)
                .save(any(Escrow.class));

        verify(paymentEventFactory)
                .paymentCompleted(
                        eq(payment),
                        any(Escrow.class)
                );

        verify(escrowEventFactory)
                .escrowCreated(any(Escrow.class));

        verify(domainEventPublisher, times(2))
                .publish(any());
    }

    @Test
    void shouldFailPaymentCreationWhenGatewayInitializationFails() {

        CreatePaymentRequest request =
                new CreatePaymentRequest(
                        bookingId,
                        "customer@example.com"
                );

        UUID customerId = UUID.randomUUID();
        UUID createdPaymentId = UUID.randomUUID();

        InternalBookingResponse booking =
                mock(InternalBookingResponse.class);

        when(booking.bookingId())
                .thenReturn(bookingId);

        when(booking.customerId())
                .thenReturn(customerId);

        when(booking.providerId())
                .thenReturn(providerId);

        when(booking.amount())
                .thenReturn(new BigDecimal("50000.00"));

        when(booking.status())
                .thenReturn(BookingStatus.QUOTE_ACCEPTED);

        when(bookingClient.getBooking(bookingId))
                .thenReturn(booking);

        when(paymentRepository.existsByBookingId(bookingId))
                .thenReturn(false);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);

                    setPaymentId(
                            payment,
                            createdPaymentId
                    );

                    return payment;
                });

        RuntimeException gatewayException =
                new RuntimeException(
                        "Paystack initialization failed"
                );

        when(paymentGatewayService.initializePayment(
                eq(createdPaymentId),
                eq("customer@example.com"),
                eq(new BigDecimal("50000.00"))
        )).thenThrow(gatewayException);

        assertThrows(
                RuntimeException.class,
                () -> paymentService.createPayment(
                        request,
                        customerId
                )
        );

        verify(bookingClient)
                .getBooking(bookingId);

        verify(paymentRepository)
                .existsByBookingId(bookingId);

        verify(paymentRepository)
                .save(any(Payment.class));

        verify(paymentGatewayService)
                .initializePayment(
                        eq(createdPaymentId),
                        eq("customer@example.com"),
                        eq(new BigDecimal("50000.00"))
                );

        verifyNoInteractions(escrowRepository);
        verifyNoInteractions(domainEventPublisher);
    }

    @Test
    void shouldAllowVerificationWhenGatewayAmountCurrencyReferenceAndStatusMatch() {

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        VerifyTransactionResponse verification =
                new VerifyTransactionResponse(
                        5_000_000L,
                        "NGN",
                        "success",
                        paymentId.toString(),
                        Instant.now().toString(),
                        "Approved"
                );

        when(paymentGatewayService.verifyPayment(paymentId.toString()))
                .thenReturn(verification);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(escrowRepository.save(any(Escrow.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(
                () -> paymentService.verifyPayment(
                        paymentId.toString()
                )
        );

        assertTrue(payment.isCompleted());

        verify(paymentGatewayService)
                .verifyPayment(paymentId.toString());

        verify(paymentRepository)
                .save(payment);

        verify(escrowRepository)
                .save(any(Escrow.class));
    }

    private void setPaymentId(
            Payment payment,
            UUID id
    ) {

        try {

            var field = Payment.class.getDeclaredField("id");

            field.setAccessible(true);

            field.set(payment, id);

        } catch (ReflectiveOperationException ex) {

            throw new IllegalStateException(
                    "Unable to set payment ID for test.",
                    ex
            );
        }
    }
}