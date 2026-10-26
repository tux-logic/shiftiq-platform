-- V18: Add workshop_id to voucher_sequences, matching VoucherSequencePersistenceEntity
-- Entity uses @IdClass(VoucherSequenceId) with @Id on (workshop_id, series) + NOT NULL workshop_id.
-- V7 created voucher_sequences with (series) as sole PK and seeded B001/F001 without workshop.
-- Backfill policy (design): assign existing rows to the default workshop id
-- (00000000-0000-0000-0000-000000000000, FiscalCorrelativeServiceImpl.DEFAULT_WORKSHOP_ID)
-- so counters are preserved without deletion.

ALTER TABLE IF EXISTS voucher_sequences DROP CONSTRAINT IF EXISTS voucher_sequences_pkey;

ALTER TABLE IF EXISTS voucher_sequences ADD COLUMN IF NOT EXISTS workshop_id UUID;

UPDATE voucher_sequences
SET workshop_id = '00000000-0000-0000-0000-000000000000'::uuid
WHERE workshop_id IS NULL;

ALTER TABLE voucher_sequences ALTER COLUMN workshop_id SET NOT NULL;

ALTER TABLE voucher_sequences ADD CONSTRAINT voucher_sequences_pkey PRIMARY KEY (workshop_id, series);

COMMENT ON COLUMN voucher_sequences.workshop_id IS 'Workshop that owns the fiscal series counter; 00000000-0000-0000-0000-000000000000 is the default workshop scope';
