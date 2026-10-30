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
import com.pm.notificationservice.application.factory.admin.AdminNotificationFactory;
import com.pm.notificationservice.application.factory.booking.BookingNotificationFactory;
import com.pm.notificationservice.application.factory.chat.ChatNotificationFactory;
import com.pm.notificationservice.application.factory.escrow.EscrowNotificationFactory;
import com.pm.notificationservice.application.factory.payment.PaymentNotificationFactory;
import com.pm.notificationservice.application.factory.rating.RatingNotificationFactory;
import com.pm.notificationservice.application.factory.system.SystemNotificationFactory;
import com.pm.notificationservice.application.factory.wallet.WalletNotificationFactory;
import com.pm.notificationservice.domain.model.Notification;
import com.pm.contracts.common.enums.RecipientRole;
import com.pm.notificationservice.domain.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationCreationServiceImpl implements NotificationCreationService {

    private final NotificationRepository notificationRepository;

    private final BookingNotificationFactory bookingNotificationFactory;

    private final PaymentNotificationFactory paymentNotificationFactory;

    private final EscrowNotificationFactory escrowNotificationFactory;

    private final WalletNotificationFactory walletNotificationFactory;

    private final RatingNotificationFactory ratingNotificationFactory;

    private final ChatNotificationFactory chatNotificationFactory;

    private final SystemNotificationFactory systemNotificationFactory;

    private final AdminNotificationFactory adminNotificationFactory;

    public NotificationCreationServiceImpl(

            NotificationRepository notificationRepository,

            BookingNotificationFactory bookingNotificationFactory,

            PaymentNotificationFactory paymentNotificationFactory,

            EscrowNotificationFactory escrowNotificationFactory,

            WalletNotificationFactory walletNotificationFactory,

            RatingNotificationFactory ratingNotificationFactory,

            ChatNotificationFactory chatNotificationFactory,

            SystemNotificationFactory systemNotificationFactory,

            AdminNotificationFactory adminNotificationFactory

    ) {

        this.notificationRepository = notificationRepository;
        this.bookingNotificationFactory = bookingNotificationFactory;
        this.paymentNotificationFactory = paymentNotificationFactory;
        this.escrowNotificationFactory = escrowNotificationFactory;
        this.walletNotificationFactory = walletNotificationFactory;
        this.ratingNotificationFactory = ratingNotificationFactory;
        this.chatNotificationFactory = chatNotificationFactory;
        this.systemNotificationFactory = systemNotificationFactory;
        this.adminNotificationFactory = adminNotificationFactory;

    }

    @Override
    public void createBookingCreatedNotification(BookingCreatedEvent event) {

        save(

                bookingNotificationFactory.bookingCreated(

                        event.customerId(),

                        RecipientRole.CUSTOMER,

                        event.bookingId()

                )

        );

    }

    @Override
    public void createBookingAcceptedNotification(BookingAcceptedEvent event) {

        save(
                bookingNotificationFactory.bookingAccepted(

                        event.customerId(),

                        RecipientRole.CUSTOMER,

                        event.bookingId()

                )

        );

    }

    @Override
    public void createBookingRejectedNotification(BookingRejectedEvent event) {

        save(

                bookingNotificationFactory.bookingRejected(

                        event.customerId(),

                        RecipientRole.CUSTOMER,

                        event.bookingId()

                )

        );

    }

    @Override
    public void createBookingCancelledNotification(BookingCancelledEvent event) {

        save(

                bookingNotificationFactory.bookingCancelled(

                        event.customerId(),

                        RecipientRole.CUSTOMER,

                        event.bookingId()

                )

        );

    }

    @Override
    public void createQuoteReceivedNotification(QuoteReceivedEvent event) {

        save(

                bookingNotificationFactory.quoteReceived(

                        event.customerId(),

                        RecipientRole.CUSTOMER,

                        event.bookingId()

                )

        );

    }

    @Override
    public void createQuoteAcceptedNotification(QuoteAcceptedEvent event) {

        save(

                bookingNotificationFactory.quoteAccepted(

                        event.providerId(),

                        RecipientRole.PROVIDER,

                        event.bookingId()

                )

        );

    }

    @Override
    public void createQuoteRejectedNotification(QuoteRejectedEvent event) {

        save(

                bookingNotificationFactory.quoteRejected(

                        event.providerId(),

                        RecipientRole.PROVIDER,

                        event.bookingId()

                )

        );

    }

    @Override
    public void createPaymentPendingNotification(PaymentPendingEvent event) {

        save(

                paymentNotificationFactory.paymentPending(

                        event.customerId(),

                        RecipientRole.CUSTOMER,

                        event.bookingId(),

                        event.paymentId(),

                        event.amount(),

                        event.currency()

                )

        );

    }

    @Override
    public void createPaymentCompletedNotification(PaymentCompletedEvent event) {

        save(

                paymentNotificationFactory.paymentCompleted(

                        event.customerId(),

                        RecipientRole.CUSTOMER,

                        event.bookingId(),

                        event.paymentId(),

                        event.amount(),

                        event.currency()

                )

        );

    }

    @Override
    public void createPaymentFailedNotification(

            PaymentFailedEvent event

    ) {

        save(

                paymentNotificationFactory.paymentFailed(

                        event.customerId(),

                        RecipientRole.CUSTOMER,

                        event.bookingId(),

                        event.paymentId(),

                        event.amount(),

                        event.currency(),

                        event.reason()

                )

        );

    }

    @Override
    public void createEscrowCreatedNotification(

            EscrowCreatedEvent event

    ) {

        save(

                escrowNotificationFactory.escrowCreated(

                        event.customerId(),

                        RecipientRole.CUSTOMER,

                        event.bookingId(),

                        event.paymentId(),

                        event.escrowId(),

                        event.totalAmount()

                )

        );

    }

    @Override
    public void createMaterialsReleasedNotification(

            MaterialsReleasedEvent event

    ) {

        // Notify Provider
        save(

                escrowNotificationFactory.materialsReleased(

                        event.providerId(),

                        RecipientRole.PROVIDER,

                        event.bookingId(),

                        event.paymentId(),

                        event.escrowId(),

                        event.amount()

                )

        );

        // Notify Customer
        save(

                escrowNotificationFactory.materialsReleased(

                        event.customerId(),

                        RecipientRole.CUSTOMER,

                        event.bookingId(),

                        event.paymentId(),

                        event.escrowId(),

                        event.amount()

                )

        );

    }

    @Override
    public void createLabourReleasedNotification(

            LabourReleasedEvent event

    ) {

        // Notify Provider
        save(

                escrowNotificationFactory.labourReleased(

                        event.providerId(),

                        RecipientRole.PROVIDER,

                        event.bookingId(),

                        event.paymentId(),

                        event.escrowId(),

                        event.amount()

                )

        );

        // Notify Customer
        save(

                escrowNotificationFactory.labourReleased(

                        event.customerId(),

                        RecipientRole.CUSTOMER,

                        event.bookingId(),

                        event.paymentId(),

                        event.escrowId(),

                        event.amount()

                )

        );

    }

    @Override
    public void createEscrowRefundedNotification(

            EscrowRefundedEvent event

    ) {

        // Notify Customer
        save(

                escrowNotificationFactory.escrowRefunded(

                        event.customerId(),

                        RecipientRole.CUSTOMER,

                        event.bookingId(),

                        event.paymentId(),

                        event.escrowId(),

                        event.amount()

                )

        );

        // Notify Provider
        save(

                escrowNotificationFactory.escrowRefunded(

                        event.providerId(),

                        RecipientRole.PROVIDER,

                        event.bookingId(),

                        event.paymentId(),

                        event.escrowId(),

                        event.amount()

                )

        );

    }

    @Override
    public void createWorkStartedNotification(
            BookingStartedEvent event
    ) {

        // Notify Customer
        save(

                bookingNotificationFactory.workStarted(

                        event.customerId(),

                        RecipientRole.CUSTOMER,

                        event.bookingId()

                )

        );

        // Notify Provider
        save(

                bookingNotificationFactory.workStarted(

                        event.providerId(),

                        RecipientRole.PROVIDER,

                        event.bookingId()

                )

        );

    }

    @Override
    public void createWorkCompletedNotification(
            BookingCompletedEvent event
    ) {

        // Notify Customer
        save(

                bookingNotificationFactory.workCompleted(

                        event.customerId(),

                        RecipientRole.CUSTOMER,

                        event.bookingId()

                )

        );

        // Notify Provider
        save(

                bookingNotificationFactory.workCompleted(

                        event.providerId(),

                        RecipientRole.PROVIDER,

                        event.bookingId()

                )

        );

    }

    @Override
    public void createCustomerConfirmedCompletionNotification(

            CustomerConfirmedCompletionEvent event

    ) {

        // Notify Provider
        save(

                bookingNotificationFactory.customerConfirmedCompletion(

                        event.providerId(),

                        RecipientRole.PROVIDER,

                        event.bookingId()

                )

        );

        // Notify Customer
        save(

                bookingNotificationFactory.customerConfirmedCompletion(

                        event.customerId(),

                        RecipientRole.CUSTOMER,

                        event.bookingId()

                )

        );

    }

    @Override
    public void createWalletCreditedNotification(

            WalletCreditedEvent event

    ) {

        save(

                walletNotificationFactory.walletCredited(

                        event.providerId(),

                        RecipientRole.PROVIDER,

                        event.walletId(),

                        event.amount()

                )

        );

    }

    @Override
    public void createWithdrawalRequestedNotification(

            WithdrawalRequestedEvent event

    ) {

        save(

                walletNotificationFactory.withdrawalRequested(

                        event.providerId(),

                        RecipientRole.PROVIDER,

                        event.walletId(),

                        event.withdrawalId(),

                        event.amount()

                )

        );

    }
    @Override
    public void createWithdrawalCompletedNotification(
            WalletWithdrawalCompletedEvent event
    ) {
        save(
                walletNotificationFactory.withdrawalCompleted(
                        event.providerId(),
                        RecipientRole.PROVIDER,
                        event.walletId(),
                        event.withdrawalId(),
                        event.amount()
                )
        );
    }
    @Override
    public void createWithdrawalCompletedNotification(
            WithdrawalCompletedEvent event
    ) {
        save(
                walletNotificationFactory.withdrawalCompleted(
                        event.providerId(),
                        RecipientRole.PROVIDER,
                        event.walletId(),
                        event.withdrawalId(),
                        event.amount()
                )
        );
    }




    @Override
    public void createWithdrawalFailedNotification(

            WithdrawalFailedEvent event

    ) {

        save(

                walletNotificationFactory.withdrawalFailed(

                        event.providerId(),

                        RecipientRole.PROVIDER,

                        event.walletId(),

                        event.withdrawalId(),

                        event.amount(),

                        event.reason()

                )

        );

    }

    @Override
    public void createProviderRatedNotification(

            ProviderRatedEvent event

    ) {

        save(

                ratingNotificationFactory.providerRated(

                        event.providerId(),

                        RecipientRole.PROVIDER,

                        event.bookingId(),

                        event.ratingId(),

                        event.rating()

                )

        );

    }

    @Override
    public void createCustomerRatedNotification(

            CustomerRatedEvent event

    ) {
        save( ratingNotificationFactory.customerRated(

                        event.customerId(),

                        RecipientRole.CUSTOMER,

                        event.bookingId(),

                        event.ratingId()

                )

        );

    }

    @Override
    public void createChatMessageNotification(

            ChatMessageReceivedEvent event

    ) {

        save(

                chatNotificationFactory.chatMessage(

                        event.recipientId(),

                        RecipientRole.CUSTOMER,

                        event.conversationId(),

                        event.senderId(),

                        event.senderName()

                )

        );

    }

    @Override
    public void createSystemNotification(

            SystemNotificationEvent event

    ) {

        save(

                systemNotificationFactory.systemNotification(

                        event.recipientUserId(),

                        event.recipientRole(),

                        event.title(),

                        event.message(),

                        event.actionUrl(),

                        event.metadata()

                )

        );

    }

    @Override
    public void createAdminNotification(

            AdminNotificationEvent event

    ) {

        save(

                adminNotificationFactory.adminNotification(

                        event.recipientUserId(),

                        event.recipientRole(),

                        event.title(),

                        event.message(),

                        event.actionUrl(),

                        event.metadata()

                )

        );

    }




    private Notification save(Notification notification) {

        Notification saved = notificationRepository.save(notification);

        // Future
        // ssePublisher.publish(saved);
        // emailDispatcher.dispatch(saved);
        // pushDispatcher.dispatch(saved);

        return saved;

    }


}