-- Align the legacy Notification Service schema with the current entities.
-- Existing OID data is only converted when the affected table is empty.
-- This migration is safe for databases where V1 already created the
-- correct TEXT columns and claimed_at column.

DO $$
DECLARE
current_type TEXT;
    row_count BIGINT;
BEGIN
    -- Fix notifications.metadata if it is still the legacy OID type.
SELECT udt_name
INTO current_type
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'notifications'
  AND column_name = 'metadata';

IF current_type = 'oid' THEN
SELECT COUNT(*)
INTO row_count
FROM public.notifications;

IF row_count > 0 THEN
            RAISE EXCEPTION
                'Cannot convert notifications.metadata: table is not empty';
END IF;

ALTER TABLE public.notifications
ALTER COLUMN metadata TYPE TEXT
            USING NULL::TEXT;
END IF;

    -- Fix outbox_events.payload if it is still the legacy OID type.
SELECT udt_name
INTO current_type
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name = 'outbox_events'
  AND column_name = 'payload';

IF current_type = 'oid' THEN
SELECT COUNT(*)
INTO row_count
FROM public.outbox_events;

IF row_count > 0 THEN
            RAISE EXCEPTION
                'Cannot convert outbox_events.payload: table is not empty';
END IF;

ALTER TABLE public.outbox_events
ALTER COLUMN payload TYPE TEXT
            USING NULL::TEXT;
END IF;
END
$$;

-- Add the missing nullable claim timestamp.
ALTER TABLE public.outbox_events
    ADD COLUMN IF NOT EXISTS claimed_at TIMESTAMP(6) WITH TIME ZONE;

-- Ensure the index used to find stale processing claims exists.
CREATE INDEX IF NOT EXISTS idx_outbox_events_processing_claimed_at
    ON public.outbox_events (claimed_at)
    WHERE status = 'PROCESSING';