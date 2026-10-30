
-- Payment Service schema for new databases.
-- Existing databases must be baselined at version 1 before V2 is applied.

CREATE TABLE payments (
                          id UUID PRIMARY KEY,
                          booking_id UUID NOT NULL,
                          customer_id UUID NOT NULL,
                          provider_id UUID NOT NULL,
                          amount NUMERIC(19, 2) NOT NULL,
                          status VARCHAR(255) NOT NULL,
                          currency VARCHAR(3) NOT NULL,
                          gateway_reference VARCHAR(255),
                          gateway_paid_at TIMESTAMPTZ,
                          created_at TIMESTAMPTZ NOT NULL,
                          completed_at TIMESTAMPTZ,
                          failed_at TIMESTAMPTZ,

                          CONSTRAINT uk_payments_booking_id UNIQUE (booking_id),
                          CONSTRAINT uk_payments_gateway_reference UNIQUE (gateway_reference),
                          CONSTRAINT payments_status_check CHECK (
                              status IN ('PENDING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUNDED')
                              )
);

CREATE INDEX idx_payment_customer ON payments (customer_id);
CREATE INDEX idx_payment_provider ON payments (provider_id);
CREATE INDEX idx_payment_status ON payments (status);


CREATE TABLE escrows (
                         id UUID PRIMARY KEY,
                         payment_id UUID NOT NULL,
                         booking_id UUID NOT NULL,
                         customer_id UUID NOT NULL,
                         provider_id UUID NOT NULL,
                         total_amount NUMERIC(19, 2) NOT NULL,
                         released_amount NUMERIC(19, 2) NOT NULL,
                         status VARCHAR(255) NOT NULL,
                         created_at TIMESTAMPTZ NOT NULL,
                         fully_released_at TIMESTAMPTZ,
                         refunded_at TIMESTAMPTZ,

                         CONSTRAINT uk_escrows_payment_id UNIQUE (payment_id),
                         CONSTRAINT uk_escrows_booking_id UNIQUE (booking_id),
                         CONSTRAINT escrows_status_check CHECK (
                             status IN ('HOLDING', 'PARTIALLY_RELEASED', 'FULLY_RELEASED', 'REFUNDED')
                             )
);

CREATE INDEX idx_escrow_booking ON escrows (booking_id);
CREATE INDEX idx_escrow_customer ON escrows (customer_id);
CREATE INDEX idx_escrow_provider ON escrows (provider_id);
CREATE INDEX idx_escrow_status ON escrows (status);


CREATE TABLE escrow_releases (
                                 id UUID PRIMARY KEY,
                                 escrow_id UUID NOT NULL,
                                 booking_id UUID NOT NULL,
                                 quote_id UUID,
                                 provider_id UUID NOT NULL,
                                 release_type VARCHAR(255) NOT NULL,
                                 amount NUMERIC(19, 2) NOT NULL,
                                 description VARCHAR(500),
                                 released_at TIMESTAMPTZ NOT NULL,

                                 CONSTRAINT escrow_releases_release_type_check CHECK (
                                     release_type IN ('MATERIALS', 'LABOUR', 'REFUND')
                                     )
);

CREATE INDEX idx_escrow_release_escrow ON escrow_releases (escrow_id);
CREATE INDEX idx_escrow_release_booking ON escrow_releases (booking_id);
CREATE INDEX idx_escrow_release_provider ON escrow_releases (provider_id);
CREATE INDEX idx_escrow_release_occurred_at ON escrow_releases (released_at);
CREATE UNIQUE INDEX uq_escrow_release_materials_booking
    ON escrow_releases (booking_id)
    WHERE release_type = 'MATERIALS';

CREATE UNIQUE INDEX uq_escrow_release_labour_booking
    ON escrow_releases (booking_id)
    WHERE release_type = 'LABOUR';


CREATE TABLE wallets (
                         id UUID PRIMARY KEY,
                         version BIGINT,
                         provider_id UUID NOT NULL,
                         available_balance NUMERIC(19, 2) NOT NULL,
                         pending_balance NUMERIC(19, 2) NOT NULL,
                         reserved_balance NUMERIC(19, 2) NOT NULL,
                         created_at TIMESTAMPTZ NOT NULL,

                         CONSTRAINT uk_wallets_provider_id UNIQUE (provider_id)
);


CREATE TABLE wallet_transactions (
                                     id UUID PRIMARY KEY,
                                     wallet_id UUID NOT NULL,
                                     provider_id UUID NOT NULL,
                                     type VARCHAR(255) NOT NULL,
                                     amount NUMERIC(19, 2) NOT NULL,
                                     reference_id UUID NOT NULL,
                                     description VARCHAR(255) NOT NULL,
                                     occurred_at TIMESTAMPTZ NOT NULL,

                                     CONSTRAINT wallet_transactions_type_check CHECK (
                                         type IN (
                                                  'CREDIT', 'RESERVE', 'RESERVE_RELEASE', 'WITHDRAWAL',
                                                  'REFUND', 'ADJUSTMENT', 'MATERIAL_ADVANCE'
                                             )
                                         )
);


CREATE TABLE withdrawals (
                             id UUID PRIMARY KEY,
                             version BIGINT,
                             wallet_id UUID NOT NULL,
                             provider_id UUID NOT NULL,
                             amount NUMERIC(19, 2) NOT NULL,
                             status VARCHAR(255) NOT NULL,
                             failure_reason VARCHAR(500),
                             requested_at TIMESTAMPTZ NOT NULL,
                             processed_at TIMESTAMPTZ,
                             completed_at TIMESTAMPTZ,
                             failed_at TIMESTAMPTZ,
                             cancelled_at TIMESTAMPTZ,

                             CONSTRAINT withdrawals_status_check CHECK (
                                 status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED')
                                 )
);

CREATE INDEX idx_withdrawal_wallet ON withdrawals (wallet_id);
CREATE INDEX idx_withdrawal_provider ON withdrawals (provider_id);
CREATE INDEX idx_withdrawal_status ON withdrawals (status);


CREATE TABLE processed_events (
                                  id UUID PRIMARY KEY,
                                  provider VARCHAR(255) NOT NULL,
                                  idempotency_key VARCHAR(255) NOT NULL,
                                  event_type VARCHAR(255) NOT NULL,
                                  processed_at TIMESTAMPTZ NOT NULL,

                                  CONSTRAINT uk_processed_event
                                      UNIQUE (provider, idempotency_key, event_type)
);

CREATE INDEX idx_processed_provider ON processed_events (provider);
CREATE INDEX idx_processed_key ON processed_events (idempotency_key);


CREATE TABLE outbox_events (
                               id UUID PRIMARY KEY,
                               aggregate_id UUID NOT NULL,
                               aggregate_type VARCHAR(255) NOT NULL,
                               event_type VARCHAR(255) NOT NULL,
                               payload TEXT NOT NULL,
                               occurred_at TIMESTAMPTZ NOT NULL,
                               status VARCHAR(255) NOT NULL,
                               published_at TIMESTAMPTZ,
                               retry_count INTEGER NOT NULL,
                               last_error VARCHAR(2000),

                               CONSTRAINT outbox_events_status_check CHECK (
                                   status IN ('PENDING', 'PUBLISHED', 'FAILED')
                                   )
);

CREATE INDEX idx_outbox_status ON outbox_events (status);
CREATE INDEX idx_outbox_occurred_at ON outbox_events (occurred_at);
