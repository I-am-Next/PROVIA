package com.pm.notificationservice.application.factory.wallet;

import com.pm.notificationservice.application.factory.shared.BaseNotificationFactory;
import com.pm.notificationservice.application.factory.shared.NotificationActionUrlFactory;
import com.pm.notificationservice.application.factory.shared.NotificationMessages;
import com.pm.notificationservice.application.factory.shared.NotificationMetadataFactory;
import com.pm.notificationservice.application.factory.shared.NotificationPriorityFactory;
import com.pm.notificationservice.application.factory.shared.NotificationTitles;
import com.pm.notificationservice.domain.model.Notification;
import com.pm.notificationservice.domain.model.enums.NotificationType;
import com.pm.contracts.common.enums.RecipientRole;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class WalletNotificationFactory extends BaseNotificationFactory {

    public WalletNotificationFactory(

            NotificationMetadataFactory metadataFactory,

            NotificationActionUrlFactory actionUrlFactory,

            NotificationPriorityFactory priorityFactory

    ) {

        super(

                metadataFactory,

                actionUrlFactory,

                priorityFactory

        );

    }

    public Notification walletCredited(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID walletId,

            BigDecimal amount

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.WALLET_CREDITED,

                NotificationTitles.WALLET_CREDITED,

                NotificationMessages.walletCredited(

                        amount

                ),

                null,

                null,

                null,

                actionUrlFactory.wallet(

                        walletId

                ),

                metadataFactory.walletMetadata(

                        walletId,

                        amount

                )

        );

    }

    public Notification withdrawalRequested(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID walletId,

            UUID withdrawalId,

            BigDecimal amount

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.WITHDRAWAL_REQUESTED,

                NotificationTitles.WITHDRAWAL_REQUESTED,

                NotificationMessages.withdrawalRequested(

                        amount

                ),

                null,

                null,

                null,

                actionUrlFactory.withdrawal(

                        withdrawalId

                ),

                metadataFactory.withdrawalMetadata(

                        walletId,

                        withdrawalId,

                        amount

                )

        );

    }

    public Notification withdrawalCompleted(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID walletId,

            UUID withdrawalId,

            BigDecimal amount

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.WITHDRAWAL_COMPLETED,

                NotificationTitles.WITHDRAWAL_COMPLETED,

                NotificationMessages.withdrawalCompleted(

                        amount

                ),

                null,

                null,

                null,

                actionUrlFactory.withdrawal(

                        withdrawalId

                ),

                metadataFactory.withdrawalMetadata(

                        walletId,

                        withdrawalId,

                        amount

                )

        );

    }

    public Notification withdrawalFailed(

            UUID recipientUserId,

            RecipientRole recipientRole,

            UUID walletId,

            UUID withdrawalId,

            BigDecimal amount,

            String reason

    ) {

        return createNotification(

                recipientUserId,

                recipientRole,

                NotificationType.WITHDRAWAL_FAILED,

                NotificationTitles.WITHDRAWAL_FAILED,

                NotificationMessages.withdrawalFailed(

                        amount,

                        reason

                ),

                null,

                null,

                null,

                actionUrlFactory.withdrawal(

                        withdrawalId

                ),

                metadataFactory.withdrawalMetadata(

                        walletId,

                        withdrawalId,

                        amount,

                        reason

                )

        );

    }

}