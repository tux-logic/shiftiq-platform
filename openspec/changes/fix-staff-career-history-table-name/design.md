# Design

## Context

The project uses Flyway (`V1`–`V16`) plus Hibernate validation (`ddl-auto=validate`, profiles dev and prod) with a custom `SnakeCaseWithPluralizedTablePhysicalNamingStrategy` that pluralizes every physical table name. Migration `V16` created `staff_career_history` and `workshop_specialty_history` in singular, so validation fails in production (Render) with `missing table [staff_career_histories]`. Entities (e.g. `StaffCareerHistoryPersistenceEntity`, `WorkshopSpecialtyHistoryPersistenceEntity`) declare singular logical `@Table` names and rely on the strategy to pluralize — this convention is used by every other table and must not change.

## Goals / Non-Goals

**Goals:**
- Make Hibernate schema validation pass for the two history tables on any database state (fresh, or pre-migrated with the singular tables).
- Avoid data loss: rename existing singular tables with their data intact.

**Non-Goals:**
- Changing the naming strategy, entity annotations, or any Java code.
- Renaming other already-converged tables (e.g. `voucher_sequences`, `workshop_specialties` are already correct because they don't conflict — `workshop_specialties` is custom plural via explicit entity mapping).
- Additive schema changes (columns, new tables).

## Decisions

1. **One new migration `V17__pluralize_history_tables.sql` instead of editing V16.**
   - V16 was already applied to the production/Aiven database; editing an applied migration breaks Flyway checksum validation. A new migration is the only safe path for existing environments, and it also covers fresh databases.
   - Alternative rejected: accepting a checksum change or re-baselining — would corrupt migration history for already-deployed databases.

2. **Rename-then-guard pattern per table** (idempotent-safe for both environments):
   - For databases where the singular table exists: `ALTER TABLE ... RENAME` moves table + data + constraints; then recreate the V16-style indexes under plural names (the old ones follow the table with old names — acceptable, or explicitly renamed) and re-apply `COMMENT ON TABLE`.
   - For fresh databases: V16 already creates singular tables (checksummed, unchangeable), so V17 still renames them — the same path works everywhere. No `CREATE ... IF NOT EXISTS` needed in V17; the negate/renames are guarded with `IF EXISTS`.
   - Alternative rejected: `ddl-auto=update` in prod — masks schema drift and is unsafe long-term.

3. **Index names pluralized** (`idx_staff_career_histories_employee_branch`, etc.) to stay consistent with table naming — `ALTER TABLE ... RENAME` does not rename indexes.

## Risks / Trade-offs

- [Windows of downtime while renaming tables] → renames are instant metadata operations in PostgreSQL; risk is negligible.
- [Flyway concurrent markers] → single migration, applied atomically per database.
- [Other environments (local devs) already migrated] → V17 applies cleanly on all of them via `IF EXISTS`.
- [Comments/indexes referencing old names] → explicitly re-applied in V17.

## Migration Plan

1. Create branch `fix/staff-career-history-table-name` from `develop` (gitflow).
2. Add `V17__pluralize_history_tables.sql`; commit as `fix(persistence): ...` conventional commit.
3. Deploy to Render; Flyway applies V17 at startup, then Hibernate validation passes.
4. Rollback strategy: V17 is forward-only; rollback would be a manual `RENAME` back to the singular names — acceptable because the app cannot run against the singular schema anyway.

## Open Questions

None.
