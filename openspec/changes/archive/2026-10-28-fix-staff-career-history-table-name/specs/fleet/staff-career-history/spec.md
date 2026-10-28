# Spec Delta: fleet/staff-career-history

## ADDED Requirements

### Requirement: Staff career snapshots persist under the pluralized physical table name

The system SHALL persist staff career-history snapshots in a physical table whose name matches the pluralized snake_case produced by the application's physical naming strategy (`staff_career_histories`), so that Hibernate schema validation passes against any database where Flyway migrations V1–V17 have been applied.

#### Scenario: Production startup validates schema successfully
- **WHEN** the application starts against a database with migrations V1–V17 applied and `spring.jpa.hibernate.ddl-auto=validate`
- **THEN** Hibernate schema validation succeeds and no missing-table error is reported for `staff_career_histories`

#### Scenario: Existing deployment with singular table is reconciled
- **WHEN** migrations V1–V17 are applied to a database that already contains the singular `staff_career_history` table and its data
- **THEN** the table and its data are renamed to `staff_career_histories` without data loss, and dependent indexes and comments reference the plural name
