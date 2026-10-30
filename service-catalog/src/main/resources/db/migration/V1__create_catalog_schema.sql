CREATE TABLE service_categories (
                                    id UUID NOT NULL,
                                    created_at TIMESTAMP(6) WITHOUT TIME ZONE,
                                    updated_at TIMESTAMP(6) WITHOUT TIME ZONE,
                                    active BOOLEAN NOT NULL,
                                    description VARCHAR(255),
                                    name VARCHAR(255) NOT NULL,

                                    CONSTRAINT service_categories_pkey
                                        PRIMARY KEY (id),

                                    CONSTRAINT uk3ik7fw1s6ksl14tdpyw9ml0t1
                                        UNIQUE (name)
);


CREATE TABLE service_items (
                               id UUID NOT NULL,
                               created_at TIMESTAMP(6) WITHOUT TIME ZONE,
                               updated_at TIMESTAMP(6) WITHOUT TIME ZONE,
                               active BOOLEAN NOT NULL,
                               category_id UUID NOT NULL,
                               description VARCHAR(255),
                               name VARCHAR(255) NOT NULL,

                               CONSTRAINT service_items_pkey
                                   PRIMARY KEY (id)
);


CREATE TABLE outbox_events (
                               id UUID NOT NULL,
                               aggregate_id VARCHAR(255),
                               aggregate_type VARCHAR(255),
                               created_at TIMESTAMP(6) WITHOUT TIME ZONE,
                               event_type VARCHAR(255),
                               next_retry_at TIMESTAMP(6) WITHOUT TIME ZONE,
                               payload OID,
                               retry_count INTEGER NOT NULL,
                               status VARCHAR(255),
                               processed_at TIMESTAMP(6) WITHOUT TIME ZONE,

                               CONSTRAINT outbox_events_pkey
                                   PRIMARY KEY (id),

                               CONSTRAINT outbox_events_status_check
                                   CHECK (
                                       status IN (
                                                  'PENDING',
                                                  'PROCESSING',
                                                  'PROCESSED',
                                                  'FAILED'
                                           )
                                       )
);