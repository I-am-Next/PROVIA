package com.pm.notificationservice.application.service;
import com.pm.contracts.admin.AdminNotificationEvent;
import com.pm.contracts.booking.*;
import com.pm.contracts.chat.ChatMessageReceivedEvent;
import com.pm.contracts.escrow.EscrowCreatedEvent;
import com.pm.contracts.escrow.EscrowRefundedEvent;
import com.pm.contracts.escrow.LabourReleasedEvent;
import com.pm.contracts.escrow.MaterialsReleasedEvent;
import com.pm.contracts.payment.PaymentCompletedEvent;
import com.pm.contracts.payment.PaymentFailedEvent;
import com.pm.contracts.payment.PaymentPendingEvent;
import com.pm.contracts.rating.CustomerRatedEvent;
import com.pm.contracts.rating.ProviderRatedEvent;
import com.pm.contracts.system.SystemNotificationEvent;
import com.pm.contracts.wallet.*;

public interface NotificationCreationService {

    /*
     * Booking
     */
    void createBookingCreatedNotification(BookingCreatedEvent event);

    void createBookingAcceptedNotification(BookingAcceptedEvent event);

    void createBookingRejectedNotification(BookingRejectedEvent event);

    void createBookingCancelledNotification(BookingCancelledEvent event);

    /*
     * Quotes
     */
    void createQuoteReceivedNotification(QuoteReceivedEvent event);

    void createQuoteAcceptedNotification(QuoteAcceptedEvent event);

    void createQuoteRejectedNotification(QuoteRejectedEvent event);

    /*
     * Payments
     */
    void createPaymentPendingNotification(PaymentPendingEvent event);

    void createPaymentCompletedNotification(PaymentCompletedEvent event);

    void createPaymentFailedNotification(PaymentFailedEvent event);

    /*
     * Escrow
     */
    void createEscrowCreatedNotification(EscrowCreatedEvent event);

    void createMaterialsReleasedNotification(MaterialsReleasedEvent event);

    void createLabourReleasedNotification(LabourReleasedEvent event);

    void createEscrowRefundedNotification(EscrowRefundedEvent event);

    /*
     * Job Progress
     */
    void createWorkStartedNotification(BookingStartedEvent event);

    void createWorkCompletedNotification(BookingCompletedEvent event);

    void createCustomerConfirmedCompletionNotification(CustomerConfirmedCompletionEvent event);

    /*
     * Wallet
     */
    void createWalletCreditedNotification(
            WalletCreditedEvent event
    );

    void createWithdrawalRequestedNotification(
            WithdrawalRequestedEvent event
    );

    void createWithdrawalCompletedNotification(
            WithdrawalCompletedEvent event
    );

    void createWithdrawalCompletedNotification(
            WalletWithdrawalCompletedEvent event
    );

    void createWithdrawalFailedNotification(
            WithdrawalFailedEvent event
    );

    /*
     * Rating
     */
    void createProviderRatedNotification(ProviderRatedEvent event);

    void createCustomerRatedNotification(CustomerRatedEvent event);

    /*
     * Chat
     */
    void createChatMessageNotification(ChatMessageReceivedEvent event);

    /*
     * System
     */
    void createSystemNotification(SystemNotificationEvent event);

    /*
     * Admin
     */
    void createAdminNotification(AdminNotificationEvent event);
}