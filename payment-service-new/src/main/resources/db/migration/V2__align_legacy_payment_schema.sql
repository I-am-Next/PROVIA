
-- Reconcile the legacy Hibernate-created schema with the managed Flyway schema.
--
-- Supported starting points:
-- 1. Existing database baselined at Flyway version 1.
-- 2. New database where V1 created payload as TEXT and added the checks.

DO $$
DECLARE
payload_type TEXT;
    legacy_oid OID;
BEGIN
SELECT c.udt_name
INTO payload_type
FROM information_schema.columns c
WHERE c.table_schema = 'public'
  AND c.table_name = 'outbox_events'
  AND c.column_name = 'payload';

IF payload_type IS NULL THEN
        RAISE EXCEPTION
            'Expected public.outbox_events.payload column was not found';

    ELSIF payload_type = 'oid' THEN

        -- Preserve the original OID references before changing the column.
        -- DISTINCT avoids unlinking a shared large object more than once.
        CREATE TEMPORARY TABLE payment_legacy_outbox_payload_oids
            ON COMMIT DROP AS
SELECT DISTINCT payload AS oid
FROM public.outbox_events
WHERE payload IS NOT NULL;

-- Read the large-object contents as UTF-8 text.
ALTER TABLE public.outbox_events
ALTER COLUMN payload TYPE TEXT
            USING convert_from(lo_get(payload), 'UTF8');

        -- Remove the old large objects after their contents have been copied.
        -- These operations participate in the migration transaction.
FOR legacy_oid IN
SELECT oid
FROM payment_legacy_outbox_payload_oids
         LOOP
    PERFORM lo_unlink(legacy_oid);
END LOOP;

DROP TABLE payment_legacy_outbox_payload_oids;

ELSIF payload_type <> 'text' THEN
        RAISE EXCEPTION
            'Unexpected outbox_events.payload type: % (expected oid or text)',
            payload_type;
END IF;
END $$;


-- Ensure the expected business CHECK constraints exist.
-- Existing constraints with these names are left unchanged.

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conrelid = 'public.payments'::regclass
           AND conname = 'payments_status_check'
    ) THEN
ALTER TABLE public.payments
    ADD CONSTRAINT payments_status_check
        CHECK (status IN (
                          'PENDING',
                          'COMPLETED',
                          'FAILED',
                          'CANCELLED',
                          'REFUNDED'
            ));
END IF;

    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conrelid = 'public.escrows'::regclass
           AND conname = 'escrows_status_check'
    ) THEN
ALTER TABLE public.escrows
    ADD CONSTRAINT escrows_status_check
        CHECK (status IN (
                          'HOLDING',
                          'PARTIALLY_RELEASED',
                          'FULLY_RELEASED',
                          'REFUNDED'
            ));
END IF;

    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conrelid = 'public.escrow_releases'::regclass
           AND conname = 'escrow_releases_release_type_check'
    ) THEN
ALTER TABLE public.escrow_releases
    ADD CONSTRAINT escrow_releases_release_type_check
        CHECK (release_type IN (
                                'MATERIALS',
                                'LABOUR',
                                'REFUND'
            ));
END IF;

    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conrelid = 'public.wallet_transactions'::regclass
           AND conname = 'wallet_transactions_type_check'
    ) THEN
ALTER TABLE public.wallet_transactions
    ADD CONSTRAINT wallet_transactions_type_check
        CHECK (type IN (
                        'CREDIT',
                        'RESERVE',
                        'RESERVE_RELEASE',
                        'WITHDRAWAL',
                        'REFUND',
                        'ADJUSTMENT',
                        'MATERIAL_ADVANCE'
            ));
END IF;

    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conrelid = 'public.withdrawals'::regclass
           AND conname = 'withdrawals_status_check'
    ) THEN
ALTER TABLE public.withdrawals
    ADD CONSTRAINT withdrawals_status_check
        CHECK (status IN (
                          'PENDING',
                          'PROCESSING',
                          'COMPLETED',
                          'FAILED',
                          'CANCELLED'
            ));
END IF;

    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conrelid = 'public.outbox_events'::regclass
           AND conname = 'outbox_events_status_check'
    ) THEN
ALTER TABLE public.outbox_events
    ADD CONSTRAINT outbox_events_status_check
        CHECK (status IN (
                          'PENDING',
                          'PUBLISHED',
                          'FAILED'
            ));
END IF;
END $$;


-- Match the JPA entity's non-null customer ID requirement.
-- Fail instead of silently modifying any existing escrow record.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
          FROM public.escrows
         WHERE customer_id IS NULL
    ) THEN
        RAISE EXCEPTION
            'Cannot enforce escrows.customer_id NOT NULL: NULL values exist. Resolve them explicitly first.';
END IF;

ALTER TABLE public.escrows
    ALTER COLUMN customer_id SET NOT NULL;
END $$;
