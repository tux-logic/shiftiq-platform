-- V19: Add series column to vouchers, matching VoucherPersistenceEntity (@Column(name = "series", length = 10))
-- Column is nullable in the entity; fiscal series/correlative live in series + correlative (correlative exists since V8).

ALTER TABLE IF EXISTS vouchers ADD COLUMN IF NOT EXISTS series VARCHAR(10);
