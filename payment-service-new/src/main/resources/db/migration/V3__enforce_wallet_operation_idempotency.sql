-- Enforce idempotency for wallet ledger operations.
-- One ledger transaction is allowed for each reference ID and operation type.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM wallet_transactions
        WHERE reference_id IS NOT NULL
          AND type IS NOT NULL
        GROUP BY reference_id, type
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION
            'Cannot apply V3: duplicate wallet transaction reference_id/type pairs exist. '
            'Inspect and reconcile duplicates before retrying the migration.';
END IF;
END
$$;

CREATE UNIQUE INDEX uq_wallet_transactions_reference_type
    ON wallet_transactions (reference_id, type);