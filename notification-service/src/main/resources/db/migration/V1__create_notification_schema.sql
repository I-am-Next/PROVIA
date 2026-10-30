-- ============================================================
-- Notification Service
-- V1: Initial PostgreSQL schema
-- ============================================================

CREATE TABLE notifications (
                               id UUID PRIMARY KEY,
                               recipient_user_id UUID NOT NULL,
                               recipient_role VARCHAR(255) NOT NULL,
                               notification_type VARCHAR(255) NOT NULL,
                               priority VARCHAR(255) NOT NULL,
                               title VARCHAR(255) NOT NULL,
                               message VARCHAR(2000) NOT NULL,
                               booking_id UUID,
                               payment_id UUID,
                               escrow_id UUID,
                               action_url VARCHAR(255) NOT NULL,
                               metadata TEXT,
                               read BOOLEAN NOT NULL DEFAULT FALSE,
                               created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                               read_at TIMESTAMP(6) WITH TIME ZONE
);

CREATE INDEX idx_notifications_recipient_created_at
    ON notifications (recipient_user_id, created_at DESC);

CREATE INDEX idx_notifications_recipient_unread
    ON notifications (recipient_user_id, read, created_at DESC);

CREATE TABLE processed_events (
                                  event_id UUID PRIMARY KEY,
                                  event_type VARCHAR(255) NOT NULL,
                                  processed_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE TABLE outbox_events (
                               id UUID PRIMARY KEY,
                               aggregate_id UUID NOT NULL,
                               aggregate_type VARCHAR(255) NOT NULL,
                               event_type VARCHAR(255) NOT NULL,
                               payload TEXT NOT NULL,
                               occurred_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                               status VARCHAR(255) NOT NULL,
                               retry_count INTEGER NOT NULL DEFAULT 0,
                               last_error VARCHAR(4000),
                               published_at TIMESTAMP(6) WITH TIME ZONE,
                               claimed_at TIMESTAMP(6) WITH TIME ZONE
);

CREATE INDEX idx_outbox_events_status_occurred_at
    ON outbox_events (status, occurred_at);

CREATE INDEX idx_outbox_events_processing_claimed_at
    ON outbox_events (claimed_at)
    WHERE status = 'PROCESSING';
