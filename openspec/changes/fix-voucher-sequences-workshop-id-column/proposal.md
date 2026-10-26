# Proposal: fix-voucher-sequences-workshop-id-column

## Why

After deploying the `fix-staff-career-history-table-name` change, startup still fails schema validation with `missing column [workshop_id] in table [voucher_sequences]`. `VoucherSequencePersistenceEntity` evolved to scope fiscal series per workshop (`workshop_id` NOT NULL), but `V7` created `voucher_sequences (series, last_correlative)` and no later migration added the column — rendered as a separate drift discovered while validating the history-table fix.

## What Changes

- Add migration `V19__add_series_to_vouchers.sql`: adds `series VARCHAR(10)` (nullable) to `vouchers` — drift discovered while validating V18 (the `VoucherPersistenceEntity.series` column never had a migration).
- **Scope expanded per user decision (option 2)**: the `vouchers.series` reconciliation folds into this change rather than spawning a separate one.
- Fix the seeded rows: decide the `workshop_id` value for the seeded `B001`/`F001` rows (may require deleting seeds the app re-seeds, or re-seeding after association exists).
- No Java change expected — the entity is the source of truth.

## Capabilities

- **New Capabilities**: none.
- **Modified Capabilities**:
  - `billing/electronic-vouchers` — adds the requirement that fiscal sequences are persisted with the workshop association expected by the persistence model, so schema validation passes.

## Impact

- **Code**: new migration `src/main/resources/db/migration/V18__add_workshop_id_to_voucher_sequences.sql`.
- **Data**: `voucher_sequences` gains a NOT NULL column; existing rows must be reconciled to satisfy it.
- **Systems**: completes the production startup fix (unblocked deploys) started by `fix-staff-career-history-table-name`.
