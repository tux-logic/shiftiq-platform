# Spec Delta: core/specialty-audit-history

## MODIFIED Requirements

### Requirement: Specialty revision entries persist under the pluralized physical table name

The system SHALL persist workshop specialty revision entries in a physical table whose name matches the pluralized snake_case produced by the application's physical naming strategy (`workshop_specialty_histories`), so that Hibernate schema validation passes against any database where Flyway migrations V1–V17 have been applied.

#### Scenario: Production startup validates schema successfully
- **WHEN** the application starts against a database with migrations V1–V17 applied and `spring.jpa.hibernate.ddl-auto=validate`
- **THEN** Hibernate schema validation succeeds and no missing-table error is reported for `workshop_specialty_histories`

#### Scenario: Existing deployment with singular table is reconciled
- **WHEN** migrations V1–V17 are applied to a database that already contains the singular `workshop_specialty_history` table and its data
- **THEN** the table and its data are renamed to `workshop_specialty_histories` without data loss, and dependent indexes and comments reference the plural name
