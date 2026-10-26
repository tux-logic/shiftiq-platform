# Tasks

## 1. Branch setup (gitflow)

- [x] 1.1 Update `develop` and create branch `fix/voucher-sequences-workshop-id-column` — NOTE: per user decision, implementing on the existing `fix/staff-career-history-table-name` branch instead

## 2. Research (resolve design open questions)

- [x] 2.1 Inspect `VoucherSequencePersistenceEntity` (ID strategy, key shape, seed behavior) and the voucher sequence seed/use code; decide backfill policy

## 3. Migration

- [x] 3.1 Create `src/main/resources/db/migration/V18__add_workshop_id_to_voucher_sequences.sql` (add column, reconcile rows, adjust PK/constraints per entity)
- [x] 3.2 Sanity-check the SQL against PostgreSQL in a rolled-back transaction (pattern used for V17)

## 3bis. Scope expansion (approved: option 2)

- [x] 3.1b Add `V19__add_series_to_vouchers.sql` (nullable `VARCHAR(10)` via `ADD COLUMN IF NOT EXISTS`)
- [x] 3.2b Sanity-check V19 SQL in a rolled-back transaction
- [x] 3.3b Update proposal/design to cover the scope expansion

## 4. Validation

- [x] 4.1 Build passes (`./mvnw clean test` — environment failures with Java 27/Mockito excluded) — NOTE: after `clean`, all 344 tests pass (0 failures, 0 errors); the earlier casualties were stale incremental bytecode, not Mockito/Java
- [x] 4.2 App starts against the real DB: Flyway applies V18/V19, `ddl-auto=validate` passes, no schema errors — FULLY VERIFIED: `V18` + `V19` applied (`now at version v19`), schema validation passed, and the app started completely (`Started ShiftiqPlatformApplication`) answering HTTP 200 on :8081

## 5. Commit and delivery

- [x] 5.1 Conventional commits (one per task with file changes) — plus two extra approved-scope fixes: `V19` series column and Spring-safe constructors for Factiliza/Brevo/EmailVerify clients
- [x] 5.2 Push branch and update open PR (#26) into `develop`
