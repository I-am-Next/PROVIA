ALTER TABLE search_inbox_events
    ADD COLUMN processing_started_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE search_inbox_events
    ADD COLUMN processing_token UUID;

CREATE INDEX idx_search_inbox_processing_started_at
    ON search_inbox_events(processing_started_at);