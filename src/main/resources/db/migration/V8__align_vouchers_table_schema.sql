-- Migration V8: Align vouchers table schema with VoucherPersistenceEntity and support offline/prepayment flow
-- 1. Add missing entity columns
ALTER TABLE vouchers ADD COLUMN IF NOT EXISTS customer_document_type VARCHAR(20);
ALTER TABLE vouchers ADD COLUMN IF NOT EXISTS customer_document_number VARCHAR(20);
ALTER TABLE vouchers ADD COLUMN IF NOT EXISTS customer_name VARCHAR(150);
ALTER TABLE vouchers ADD COLUMN IF NOT EXISTS external_invoice_id UUID;
ALTER TABLE vouchers ADD COLUMN IF NOT EXISTS pdf_url VARCHAR(500);
ALTER TABLE vouchers ADD COLUMN IF NOT EXISTS correlative VARCHAR(10);

-- 2. Explicitly drop NOT NULL on prepayment and offline fields to ensure existing tables won't reject phase 1 inserts
ALTER TABLE vouchers ALTER COLUMN external_invoice_id DROP NOT NULL;
ALTER TABLE vouchers ALTER COLUMN pdf_url DROP NOT NULL;
ALTER TABLE vouchers ALTER COLUMN customer_document_type DROP NOT NULL;
ALTER TABLE vouchers ALTER COLUMN customer_document_number DROP NOT NULL;
ALTER TABLE vouchers ALTER COLUMN customer_name DROP NOT NULL;
ALTER TABLE vouchers ALTER COLUMN correlative DROP NOT NULL;

-- 3. Drop NOT NULL constraints on legacy/unmapped columns from V1 baseline to allow clean aggregate persistence
ALTER TABLE vouchers ALTER COLUMN branch_id DROP NOT NULL;
ALTER TABLE vouchers ALTER COLUMN voucher_number DROP NOT NULL;
ALTER TABLE vouchers ALTER COLUMN subtotal_amount DROP NOT NULL;
ALTER TABLE vouchers ALTER COLUMN currency DROP NOT NULL;
ALTER TABLE vouchers ALTER COLUMN created_by DROP NOT NULL;
