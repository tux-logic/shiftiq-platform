-- Verify no duplicate vouchers exist before applying UNIQUE constraint.
-- Aborts migration if duplicate quote_id records are found to prevent silent deletion of fiscal/accounting documents.
DO $$
DECLARE
    duplicate_count INT;
BEGIN
    SELECT COUNT(*) INTO duplicate_count FROM (
        SELECT quote_id FROM vouchers GROUP BY quote_id HAVING COUNT(*) > 1
    ) duplicates;

    IF duplicate_count > 0 THEN
        RAISE EXCEPTION 'Flyway migration V6 aborted: Detected % quotes with duplicate vouchers. Manual resolution/audit required before adding UNIQUE constraint.', duplicate_count;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_vouchers_quote_id'
    ) THEN
        ALTER TABLE vouchers ADD CONSTRAINT uq_vouchers_quote_id UNIQUE (quote_id);
    END IF;
END $$;
