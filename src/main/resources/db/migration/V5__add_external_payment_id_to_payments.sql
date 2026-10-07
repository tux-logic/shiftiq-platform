-- Migration V5: Add external payment ID and provider to payments table with UNIQUE constraint for replay prevention
ALTER TABLE payments
    ADD COLUMN payment_provider VARCHAR(50) DEFAULT 'MANUAL',
    ADD COLUMN external_payment_id VARCHAR(255);

CREATE UNIQUE INDEX uq_payments_provider_external_id
    ON payments (payment_provider, external_payment_id)
    WHERE external_payment_id IS NOT NULL;
