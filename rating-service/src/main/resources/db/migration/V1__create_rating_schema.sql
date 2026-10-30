CREATE TABLE ratings (
                         id UUID NOT NULL,
                         booking_id UUID NOT NULL,
                         comment VARCHAR(1000),
                         created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                         reviewee_role VARCHAR(255) NOT NULL,
                         reviewee_user_id UUID NOT NULL,
                         reviewer_role VARCHAR(255) NOT NULL,
                         reviewer_user_id UUID NOT NULL,
                         score INTEGER NOT NULL,
                         updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,

                         CONSTRAINT ratings_pkey
                             PRIMARY KEY (id),

                         CONSTRAINT uk_rating_booking_reviewer_reviewee
                             UNIQUE (
                                     booking_id,
                                     reviewer_user_id,
                                     reviewee_user_id
                                 ),

                         CONSTRAINT ratings_reviewee_role_check
                             CHECK (
                                 reviewee_role IN ('CUSTOMER', 'PROVIDER')
                                 ),

                         CONSTRAINT ratings_reviewer_role_check
                             CHECK (
                                 reviewer_role IN ('CUSTOMER', 'PROVIDER')
                                 )
);

CREATE TABLE outbox_events (
                               id UUID NOT NULL,
                               aggregate_id UUID NOT NULL,
                               aggregate_type VARCHAR(255) NOT NULL,
                               event_type VARCHAR(255) NOT NULL,
                               last_error VARCHAR(2000),
                               occurred_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
                               payload TEXT NOT NULL,
                               published_at TIMESTAMP(6) WITH TIME ZONE,
                               retry_count INTEGER NOT NULL,
                               status VARCHAR(255) NOT NULL,

                               CONSTRAINT outbox_events_pkey
                                   PRIMARY KEY (id),

                               CONSTRAINT outbox_events_status_check
                                   CHECK (
                                       status IN ('PENDING', 'PUBLISHED', 'FAILED')
                                       )
);

CREATE INDEX idx_outbox_status_occurred_at
    ON outbox_events (status, occurred_at);