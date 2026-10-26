# Design

## Context

`VoucherSequencePersistenceEntity` maps `@Column(name = "workshop_id", nullable = false)` but `V7` created `voucher_sequences (series VARCHAR(10) PRIMARY KEY, last_correlative INT)` and seeds `B001`/`F001`. Hibernate validate fails on the missing column. Discovered during validation of the `fix-staff-career-history-table-name` change; kept separate per user decision (option 1).

## Goals / Non-Goals

**Goals:**
- Add `workshop_id` to `voucher_sequences` matching the entity expectations exactly (type, nullability, composite key layout).
- Reconcile seeded rows so the NOT NULL constraint holds.

**Non-Goals:**
- Changes to application logic for sequence resolution (out of scope; only schema is drifted).
- Renaming other tables/columns.

## Decisions

To be resolved while applying (verify entity mapping first, they supersede these sketches):
1. **PK shape**: the entity uses `@IdClass(VoucherSequenceId)` with `@Id` on both `workshop_id` and `series` → composite PK `(workshop_id, series)`. V18 drops the `series`-only PK and recreates the composite.
2. **Backfill policy**: sequences are created on demand (`FiscalCorrelativeServiceImpl.nextCorrelative`), never re-seeded at startup; `DEFAULT_WORKSHOP_ID` is the zero UUID `00000000-0000-0000-0000-000000000000`. Backfill existing `B001`/`F001` rows (both counters = 0) to that default workshop id — no deletion, counters preserved.
3. **V19 for `vouchers.series`**: validation of V18 revealed the same drift pattern one table over (`VoucherPersistenceEntity.series` has no migration). Per user decision, a `V19__add_series_to_vouchers.sql` (nullable `VARCHAR(10)`, `IF NOT EXISTS`-guarded) is included in this change. Entity-vs-DB column comparison (parsed from all persistence entities against `information_schema`) confirms it is the last real drift — remaining flags are `@ElementCollection` columns that live in child tables (`user_branches`, `work_order_entry_inspection_images`, `work_order_task_evidence_images`), all present in the DB.

## Risks / Trade-offs

- [Deleting seeded rows loses correlative counters] → check for deployed usage; production counters may hold real correlatives that must not reset. Mitigation: backfill with a real workshop id when possible rather than delete.
- [Flyway checksums] → new migration V18 only; never edit V7.

## Migration Plan

`V18` (atomic): alter + reconcile rows. Rollback: manual `ALTER TABLE ... DROP COLUMN workshop_id` (data loss acceptable only if deployed usage confirms).

## Open Questions

- Does the entity use a composite ID or a separate unique constraint on `(workshop_id, series)`?
- Where do seeded `B001`/`F001` belong when the column becomes NOT NULL?
