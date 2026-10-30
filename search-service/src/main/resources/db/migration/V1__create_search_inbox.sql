CREATE TABLE search_inbox_events (
                                     event_id UUID PRIMARY KEY,

                                     topic VARCHAR(255) NOT NULL,

                                     event_type VARCHAR(255) NOT NULL,

                                     event_version INTEGER NOT NULL,

                                     aggregate_id UUID,

                                     payload TEXT NOT NULL,

                                     status VARCHAR(32) NOT NULL,

                                     received_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                     processed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_search_inbox_status
    ON search_inbox_events(status);

CREATE INDEX idx_search_inbox_aggregate_status
    ON search_inbox_events(aggregate_id, status);

CREATE INDEX idx_search_inbox_received_at
    ON search_inbox_events(received_at);