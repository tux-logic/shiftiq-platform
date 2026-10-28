# core/specialty-audit-history Specification

## Purpose
Provides immutable revision logging and historical inspection of workshop specialty catalog definitions across name, code, description, and status changes.

## Requirements

### Requirement: Record specialty revision log on definition changes
The system SHALL record an immutable audit entry whenever a workshop specialty is seeded, created, updated, or deactivated.

#### Scenario: Revision log on specialty creation
- **WHEN** a new specialty is added or default specialties are seeded for a workshop
- **THEN** an audit revision is recorded capturing the initial name, code, description, and active status

#### Scenario: Revision log on specialty update
- **WHEN** an authorized workshop owner updates the name or description of a specialty
- **THEN** a new audit revision is appended capturing the new attributes, modification timestamp, and acting user ID

#### Scenario: Revision log on specialty deactivation
- **WHEN** an authorized workshop owner deactivates a specialty
- **THEN** an audit revision is appended recording the inactive status and deactivation timestamp

### Requirement: Query workshop specialty revision history
The system SHALL provide an authorized query endpoint to retrieve the revision history of workshop specialties.

#### Scenario: Retrieve revisions for a specific specialty
- **WHEN** an authorized workshop owner requests the history of a specific specialty ID
- **THEN** the system returns all recorded revisions for that specialty ordered chronologically by recorded date descending

#### Scenario: Unauthorized workshop access denied
- **WHEN** a user without authorization for the workshop requests specialty revision history
- **THEN** the system rejects the request with HTTP 403 Forbidden

### Requirement: Specialty revision entries persist under the pluralized physical table name

The system SHALL persist workshop specialty revision entries in a physical table whose name matches the pluralized snake_case produced by the application's physical naming strategy (`workshop_specialty_histories`), so that Hibernate schema validation passes against any database where Flyway migrations V1–V17 have been applied.

#### Scenario: Production startup validates schema successfully
- **WHEN** the application starts against a database with migrations V1–V17 applied and `spring.jpa.hibernate.ddl-auto=validate`
- **THEN** Hibernate schema validation succeeds and no missing-table error is reported for `workshop_specialty_histories`

#### Scenario: Existing deployment with singular table is reconciled
- **WHEN** migrations V1–V17 are applied to a database that already contains the singular `workshop_specialty_history` table and its data
- **THEN** the table and its data are renamed to `workshop_specialty_histories` without data loss, and dependent indexes and comments reference the plural name
