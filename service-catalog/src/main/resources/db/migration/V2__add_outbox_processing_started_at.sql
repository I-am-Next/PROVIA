ALTER TABLE outbox_events
    ADD COLUMN processing_started_at TIMESTAMP NULL;

CREATE INDEX idx_outbox_events_processing_started_at
    ON outbox_events (processing_started_at)
    WHERE status = 'PROCESSING';
