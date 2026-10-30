package com.pm.paymentservicenew.application.service;

import com.pm.paymentservicenew.application.dto.request.CreatePaymentRequest;
import com.pm.paymentservicenew.domain.model.escrow.Escrow;
import com.pm.paymentservicenew.domain.model.payment.Payment;
import com.pm.paymentservicenew.domain.repository.EscrowRepository;
import com.pm.paymentservicenew.domain.repository.PaymentRepository;
import com.pm.paymentservicenew.integration.dto.BookingStatus;
import com.pm.paymentservicenew.integration.dto.InternalBookingResponse;
import com.pm.paymentservicenew.integration.paystack.service.PaymentGatewayService;
import com.pm.paymentservicenew.infrastructure.client.BookingClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class PaymentServiceTransactionIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EscrowRepository escrowRepository;

    @MockBean
    private BookingClient bookingClient;

    @MockBean
    private PaymentGatewayService paymentGatewayService;

    @SpyBean
    private EscrowRepository escrowRepositorySpy;

    private UUID customerId;
    private UUID providerId;
    private UUID bookingId;

    @DynamicPropertySource
    static void testProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "internal.service-key",
                () -> "integration-test-internal-key"
        );
    }

    @BeforeEach
    void setUp() {

        paymentRepository.deleteAll();
        escrowRepository.deleteAll();

        customerId = UUID.randomUUID();
        providerId = UUID.randomUUID();
        bookingId = UUID.randomUUID();
    }

    @Test
    void createPayment_shouldRollbackPaymentWhenGatewayInitializationFails() {

        BigDecimal amount =
                new BigDecimal("50000.00");

        InternalBookingResponse booking =
                new InternalBookingResponse(
                        bookingId,
                        customerId,
                        providerId,
                        amount,
                        BookingStatus.QUOTE_ACCEPTED
                );

        when(bookingClient.getBooking(bookingId))
                .thenReturn(booking);

        when(paymentGatewayService.initializePayment(
                any(UUID.class),
                anyString(),
                eq(amount)
        )).thenThrow(
                new RuntimeException(
                        "Payment gateway initialization failed"
                )
        );

        CreatePaymentRequest request =
                new CreatePaymentRequest(
                        bookingId,
                        "customer@example.com"
                );

        assertThrows(
                RuntimeException.class,
                () -> paymentService.createPayment(
                        request,
                        customerId
                )
        );

        /*
         * The payment was saved before the gateway call.
         *
         * Because PaymentServiceImpl is @Transactional,
         * the gateway exception must cause the transaction
         * to roll back.
         */
        assertFalse(
                paymentRepository.existsByBookingId(bookingId)
        );

        assertTrue(
                paymentRepository.findByBookingId(bookingId)
                        .isEmpty()
        );

        verify(paymentGatewayService)
                .initializePayment(
                        any(UUID.class),
                        eq("customer@example.com"),
                        eq(amount)
                );
    }

    @Test
    void verifyPayment_shouldRollbackPaymentCompletionWhenEscrowPersistenceFails() {

        BigDecimal amount =
                new BigDecimal("50000.00");

        Payment payment =
                Payment.create(
                        bookingId,
                        customerId,
                        providerId,
                        amount
                );

        paymentRepository.saveAndFlush(payment);

        String reference =
                payment.getId().toString();

        var verification =
                new com.pm.paymentservicenew.integration.paystack.dto.VerifyTransactionResponse(
                        5_000_000L,
                        "NGN",
                        "success",
                        reference,
                        "2026-09-28T00:00:00Z",
                        "Approved"
                );

        when(paymentGatewayService.verifyPayment(reference))
                .thenReturn(verification);

        /*
         * The payment will be completed and saved first.
         *
         * Escrow persistence then fails.
         *
         * The exception must propagate out of the
         * @Transactional service method, causing the
         * entire transaction to roll back.
         */
        doThrow(
                new RuntimeException(
                        "Escrow persistence failed"
                )
        ).when(escrowRepositorySpy)
                .save(any(Escrow.class));

        assertThrows(
                RuntimeException.class,
                () -> paymentService.verifyPayment(reference)
        );

        /*
         * Clear the persistence context so that we read
         * the database rather than an already-managed entity.
         */
        paymentRepository.flush();

        Payment persistedPayment =
                paymentRepository.findById(payment.getId())
                        .orElse(null);

        /*
         * The payment must still be PENDING because the
         * COMPLETED state transition was rolled back.
         */
        assertNotNull(persistedPayment);

        assertTrue(
                persistedPayment.isPending()
        );

        assertNull(
                persistedPayment.getGatewayReference()
        );

        assertNull(
                persistedPayment.getCompletedAt()
        );

        /*
         * No escrow should exist because the transaction
         * containing the payment completion and escrow
         * creation was rolled back.
         */
        assertTrue(
                escrowRepository
                        .findByPaymentId(payment.getId())
                        .isEmpty()
        );

        verify(paymentGatewayService)
                .verifyPayment(reference);

        verify(escrowRepositorySpy)
                .save(any(Escrow.class));
    }
}