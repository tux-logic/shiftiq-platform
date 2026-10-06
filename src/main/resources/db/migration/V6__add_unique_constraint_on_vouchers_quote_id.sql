-- Deduplicate any historical vouchers with duplicate quote_id (keeping the latest voucher)
DELETE FROM payments
WHERE voucher_id IN (
    SELECT v1.id FROM vouchers v1
    JOIN vouchers v2 ON v1.quote_id = v2.quote_id AND (v1.created_at < v2.created_at OR (v1.created_at = v2.created_at AND v1.id < v2.id))
);

DELETE FROM vouchers v1
USING vouchers v2
WHERE v1.quote_id = v2.quote_id AND (v1.created_at < v2.created_at OR (v1.created_at = v2.created_at AND v1.id < v2.id));

-- Add UNIQUE constraint on quote_id safely
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_vouchers_quote_id'
    ) THEN
        ALTER TABLE vouchers ADD CONSTRAINT uq_vouchers_quote_id UNIQUE (quote_id);
    END IF;
END $$;
