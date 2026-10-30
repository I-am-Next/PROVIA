package com.pm.searchservice.infrastructure.idempotency;

import com.pm.searchservice.infrastructure.consumer.SearchEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PendingEventReplayService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    PendingEventReplayService.class
            );

    private final InboxEventRepository inboxRepository;
    private final EventInboxService inboxService;
    private final SearchEventProcessor eventProcessor;

    public PendingEventReplayService(
            InboxEventRepository inboxRepository,
            EventInboxService inboxService,
            SearchEventProcessor eventProcessor
    ) {
        this.inboxRepository = inboxRepository;
        this.inboxService = inboxService;
        this.eventProcessor = eventProcessor;
    }

    @Scheduled(
            fixedDelayString =
                    "${search.inbox.replay-delay:10000}"
    )
    public void replayPendingEvents() {

        inboxService.requeueStaleProcessing();

        List<InboxEvent> events =
                inboxRepository
                        .findTop100ByStatusOrderByReceivedAtAsc(
                                InboxEventStatus.PENDING
                        );

        for (InboxEvent event : events) {

            replay(event);
        }
    }

    private void replay(
            InboxEvent event
    ) {

        Optional<UUID> token =
                inboxService.claimForReplay(
                        event.getEventId()
                );

        if (token.isEmpty()) {
            return;
        }

        UUID processingToken =
                token.get();

        try {

            eventProcessor.processFromInbox(
                    event
            );

            inboxService.markProcessed(
                    event.getEventId(),
                    processingToken
            );

        } catch (Exception ex) {

            try {

                inboxService.markPending(
                        event.getEventId(),
                        processingToken
                );

            } catch (Exception stateException) {

                log.error(
                        "Failed to return inbox event to PENDING. "
                                + "eventId={}",
                        event.getEventId(),
                        stateException
                );
            }

            log.warn(
                    "Pending Search event replay failed. "
                            + "eventId={}, topic={}, type={}",
                    event.getEventId(),
                    event.getTopic(),
                    event.getEventType(),
                    ex
            );
        }
    }
}