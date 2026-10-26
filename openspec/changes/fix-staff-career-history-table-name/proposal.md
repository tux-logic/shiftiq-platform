# Proposal: fix-staff-career-history-table-name

## Why

Deploying to production (Render) fails at startup: Hibernate's `SchemaManagementException: Schema validation: missing table [staff_career_histories]`. The `SnakeCaseWithPluralizedTablePhysicalNamingStrategy` pluralizes every physical table name, but migrations `V16` create `staff_career_history` and `workshop_specialty_history` in **singular**, so schema validation of the two history tables always fails against any database where migrations ran. This blocks the production deployment.

## What Changes

- Add migration `V17__pluralize_history_tables.sql` that renames `staff_career_history` → `staff_career_histories` and `workshop_specialty_history` → `workshop_specialty_histories`, including their indexes, comments, and any foreign-key references (rename is idempotent-guarded via `IF EXISTS`).
- Correct `V16`'s created names going forward is **not** possible retroactively; instead V17 handles both the pre-applied case (rename) and a fresh database (rename no-ops, but tables must exist) — so V17 creates tables with plural names using `CREATE TABLE IF NOT EXISTS` semantics guarded by a rename-or-create flow.
- Align comments and index names with the new table names.
- No entity code changes: entities keep `@Table(name = "staff_career_history")` singular logical names; the pluralization naming strategy will resolve them to the plural physical names.

## Capabilities

- **New Capabilities**: none.
- **Modified Capabilities**:
  - `fleet/staff-career-history` — adds the requirement that the persisted career-history snapshots live in physical tables that match Hibernate's pluralized physical naming, so schema validation passes.
  - `core/specialty-audit-history` — same alignment requirement for the workshop specialty revision history tables.

## Impact

- **Code**: new Flyway migration `src/main/resources/db/migration/V17__pluralize_history_tables.sql`. Optionally annotate the two entities' `@Table` documentation comments; no Java logic changes.
- **Data**: table names change (rename, not data loss; constraints and indexes follow the table rename). Indexes/constraints that reference the singular names inside V16 must be recreated/renamed to the plural names.
- **Systems**: production (Render) startup unblocked; fresh dev databases converge with existing ones after migrations V1–V17.
