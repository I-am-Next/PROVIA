
-- ============================================================
-- BOOKING SERVICE
-- V1: Initial schema
--
-- Intended for a fresh PostgreSQL database.
-- This migration replaces the previous V1 + V2 setup.
-- ============================================================


-- ============================================================
-- BOOKINGS
-- ============================================================

CREATE TABLE bookings (
                          id UUID NOT NULL,
                          user_id UUID NOT NULL,
                          provider_user_id UUID NOT NULL,
                          provider_service_id UUID NOT NULL,

                          service_name VARCHAR(255) NOT NULL,
                          price NUMERIC(19, 2) NOT NULL,
                          description VARCHAR(1000),
                          notes VARCHAR(1000),

                          address_line1 VARCHAR(255) NOT NULL,
                          address_line2 VARCHAR(255),
                          city VARCHAR(255) NOT NULL,
                          state VARCHAR(255),
                          postal_code VARCHAR(255),
                          country VARCHAR(255) NOT NULL,

                          scheduled_start TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                          scheduled_end TIMESTAMP WITHOUT TIME ZONE NOT NULL,

                          status VARCHAR(255) NOT NULL,

                          accepted_at TIMESTAMP WITHOUT TIME ZONE,
                          started_at TIMESTAMP WITHOUT TIME ZONE,
                          completed_at TIMESTAMP WITHOUT TIME ZONE,
                          cancelled_at TIMESTAMP WITHOUT TIME ZONE,

                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                          updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                          version BIGINT,

                          CONSTRAINT pk_bookings
                              PRIMARY KEY (id),

                          CONSTRAINT chk_bookings_price_non_negative
                              CHECK (price >= 0),

                          CONSTRAINT chk_bookings_schedule
                              CHECK (scheduled_end > scheduled_start)
);


-- ============================================================
-- BOOKING QUOTES
-- ============================================================

CREATE TABLE booking_quotes (
                                id UUID NOT NULL,
                                booking_id UUID NOT NULL,
                                provider_id UUID NOT NULL,

                                labour_amount NUMERIC(19, 2) NOT NULL,
                                materials_amount NUMERIC(19, 2) NOT NULL,
                                materials_released BOOLEAN NOT NULL DEFAULT FALSE,
                                service_fee NUMERIC(19, 2) NOT NULL,
                                total_amount NUMERIC(19, 2) NOT NULL,

                                description VARCHAR(2000),
                                estimated_duration_days INTEGER NOT NULL,
                                status VARCHAR(255) NOT NULL,

                                expires_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                CONSTRAINT pk_booking_quotes
                                    PRIMARY KEY (id),

                                CONSTRAINT fk_booking_quotes_booking
                                    FOREIGN KEY (booking_id)
                                        REFERENCES bookings (id),

                                CONSTRAINT chk_booking_quotes_labour_non_negative
                                    CHECK (labour_amount >= 0),

                                CONSTRAINT chk_booking_quotes_materials_non_negative
                                    CHECK (materials_amount >= 0),

                                CONSTRAINT chk_booking_quotes_service_fee_non_negative
                                    CHECK (service_fee >= 0),

                                CONSTRAINT chk_booking_quotes_total_non_negative
                                    CHECK (total_amount >= 0),

                                CONSTRAINT chk_booking_quotes_duration_positive
                                    CHECK (estimated_duration_days > 0),

                                CONSTRAINT chk_booking_quotes_total
                                    CHECK (
                                        total_amount =
                                        labour_amount +
                                        materials_amount +
                                        service_fee
                                        ),

                                CONSTRAINT booking_quotes_status_check
                                    CHECK (
                                        status IN (
                                                   'PENDING',
                                                   'ACCEPTED',
                                                   'REJECTED',
                                                   'EXPIRED'
                                            )
                                        )
);


-- Only one PENDING quote may exist per booking.
-- Quotes in other statuses remain as history.
CREATE UNIQUE INDEX uq_booking_quotes_pending
    ON booking_quotes (booking_id)
    WHERE status = 'PENDING';


-- ============================================================
-- BOOKING STATUS HISTORY
-- ============================================================

CREATE TABLE booking_status_history (
                                        id UUID NOT NULL,
                                        booking_id UUID NOT NULL,

                                        status VARCHAR(255) NOT NULL,
                                        changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                        changed_by UUID NOT NULL,
                                        note VARCHAR(500),

                                        CONSTRAINT pk_booking_status_history
                                            PRIMARY KEY (id),

                                        CONSTRAINT fk_booking_status_history_booking
                                            FOREIGN KEY (booking_id)
                                                REFERENCES bookings (id),

                                        CONSTRAINT booking_status_history_status_check
                                            CHECK (
                                                status IN (
                                                           'REQUESTED',
                                                           'REJECTED',
                                                           'AWAITING_QUOTE',
                                                           'QUOTE_SENT',
                                                           'QUOTE_ACCEPTED',
                                                           'QUOTE_REJECTED',
                                                           'PAYMENT_COMPLETED',
                                                           'MATERIALS_RELEASED',
                                                           'IN_PROGRESS',
                                                           'COMPLETED',
                                                           'COMPLETION_CONFIRMED',
                                                           'CANCELLED'
                                                    )
                                                )
);


-- ============================================================
-- OUTBOX EVENTS
-- ============================================================

CREATE TABLE outbox_events (
                               id UUID NOT NULL,
                               aggregate_id UUID NOT NULL,
                               aggregate_type VARCHAR(50) NOT NULL,
                               event_type VARCHAR(100) NOT NULL,
                               event_version INTEGER NOT NULL,

    -- Serialized event payload stored as PostgreSQL BYTEA.
                               payload BYTEA NOT NULL,

                               occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
                               status VARCHAR(255) NOT NULL,
                               published_at TIMESTAMP WITH TIME ZONE,

                               retry_count INTEGER NOT NULL DEFAULT 0,
                               last_error VARCHAR(2000),

                               CONSTRAINT pk_outbox_events
                                   PRIMARY KEY (id),

                               CONSTRAINT chk_outbox_event_version
                                   CHECK (event_version > 0),

                               CONSTRAINT chk_outbox_retry_count
                                   CHECK (retry_count >= 0),

                               CONSTRAINT outbox_events_status_check
                                   CHECK (
                                       status IN (
                                                  'PENDING',
                                                  'PUBLISHED',
                                                  'FAILED'
                                           )
                                       )
);


-- ============================================================
-- PROCESSED EVENTS / INBOX
-- ============================================================

CREATE TABLE processed_events (
                                  event_id UUID NOT NULL,
                                  event_type VARCHAR(255),
                                  processed_at TIMESTAMP WITH TIME ZONE,

                                  CONSTRAINT pk_processed_events
                                      PRIMARY KEY (event_id)
);


-- ============================================================
-- BOOKING INDEXES
-- ============================================================

CREATE INDEX idx_booking_user
    ON bookings (user_id);

CREATE INDEX idx_booking_provider
    ON bookings (provider_user_id);

CREATE INDEX idx_booking_status
    ON bookings (status);

CREATE INDEX idx_booking_provider_status
    ON bookings (provider_user_id, status);


-- ============================================================
-- BOOKING QUOTE INDEXES
-- ============================================================

CREATE INDEX idx_quote_booking
    ON booking_quotes (booking_id);

CREATE INDEX idx_quote_provider
    ON booking_quotes (provider_id);


-- ============================================================
-- BOOKING STATUS HISTORY INDEXES
-- ============================================================

CREATE INDEX idx_booking_history_booking
    ON booking_status_history (booking_id);

CREATE INDEX idx_booking_history_changed_at
    ON booking_status_history (changed_at);


-- ============================================================
-- OUTBOX INDEXES
-- ============================================================

CREATE INDEX idx_outbox_status
    ON outbox_events (status);

CREATE INDEX idx_outbox_occurred_at
    ON outbox_events (occurred_at);

CREATE INDEX idx_outbox_aggregate
    ON outbox_events (aggregate_type, aggregate_id);
