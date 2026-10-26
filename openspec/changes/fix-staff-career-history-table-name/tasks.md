# Tasks

## 1. Branch setup (gitflow)

- [x] 1.1 Fetch and checkout the latest `develop`; confirm clean working tree
- [x] 1.2 Create branch `fix/staff-career-history-table-name` from `develop`

## 2. Migration

- [x] 2.1 Create `src/main/resources/db/migration/V17__pluralize_history_tables.sql`:
      - Rename `staff_career_history` → `staff_career_histories` (guarded with `IF EXISTS`)
      - Rename `workshop_specialty_history` → `workshop_specialty_histories` (guarded with `IF EXISTS`)
      - Recreate indexes with pluralized names (drop old-named indexes that followed the rename)
      - Re-apply `COMMENT ON TABLE` for both plural tables
- [ ] 2.2 Sanity-check the SQL against PostgreSQL (renames + indexes + comments syntax)

## 3. Validation

- [x] 3.1 Run `./mvnw test` (or the project's build command) to confirm compilation and context tests pass — NOTE: compilation OK (344 tests run); 40 test errors are pre-existing environment issues (Java 27 + Mockito inline mocks) reproduced identically on `develop`
- [x] 3.2 Against a local/dev database with V1–V16 applied, start the app: confirm Flyway applies V17 and `ddl-auto=validate` passes without the missing-table error — NOTE: Flyway applied V17 successfully on the real Aiven DB (`now at version v17`); the original `missing table [staff_career_histories]` error is gone. NEW finding: startup still fails with a different pre-existing drift: `missing column [workshop_id] in table [voucher_sequences]` (out of scope of this change — see pause report)

## 4. Commit and delivery

- [x] 4.1 Commit with a conventional commit, e.g. `fix(persistence): align history tables with pluralized naming strategy`
- [ ] 4.2 Push the branch and open a PR into `develop`
