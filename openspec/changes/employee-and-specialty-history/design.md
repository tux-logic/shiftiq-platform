# Design

## Context

See `proposal.md` for motivation. Currently, `EmployeeRegistration` in `fleet` and `WorkshopSpecialty` in `core` overwrite attributes during updates. While `AuditableAbstractPersistenceEntity` tracks timestamps, intermediate states are lost.

## Goals / Non-Goals

**Goals:**
- Provide immutable, append-only historical audit records for staff branch career transitions (specialty, salary, status) and workshop specialty catalog modifications.
- Deliver dedicated REST APIs to query history chronologically with multi-tenant security enforcement.
- Maintain DDD bounded context separation (`fleet` owns staff career history, `core` owns specialty audit history).

**Non-Goals:**
- Full-database audit logs (Envers/JaVers) for unrelated domains (e.g. inventory or quotes).
- Retroactive historical data generation for past modifications that took place before this schema migration.
- Modifying the personal identity `Employee` aggregate in `core`.

## Decisions

### 1. Dedicated History Entities vs. Generic Envers/Audit Table
- **Choice**: Implement explicit JPA persistence entities (`StaffCareerHistoryPersistenceEntity` and `WorkshopSpecialtyHistoryPersistenceEntity`) backed by dedicated tables.
- **Rationale**: Keeps domain models clean and explicit, avoids heavy third-party framework magic or reflection overhead, permits typed queries with custom indexes, and enables domain-meaningful attributes like `changeType` and `changedBy`.
- **Alternatives Considered**: Hibernate Envers (adds excessive shadow tables and complex joins) or a generic JSON `audit_logs` table (sacrifices schema validation and relational query performance).

### 2. Flyway Migration `V16__create_staff_and_specialty_history_tables.sql`
- **Schema**:
  - `staff_career_history`: `id (UUID PK)`, `registration_id (UUID)`, `employee_id (UUID)`, `branch_id (UUID)`, `speciality (VARCHAR)`, `speciality_name (VARCHAR)`, `salary (NUMERIC/DOUBLE PRECISION)`, `status (VARCHAR)`, `change_type (VARCHAR)`, `changed_by (UUID)`, `recorded_at (TIMESTAMPTZ)`.
  - `workshop_specialty_history`: `id (UUID PK)`, `specialty_id (UUID)`, `workshop_id (UUID)`, `name (VARCHAR)`, `code (VARCHAR)`, `description (TEXT)`, `active (BOOLEAN)`, `change_type (VARCHAR)`, `changed_by (UUID)`, `recorded_at (TIMESTAMPTZ)`.
- **Indexes**: Composite index on `(branch_id, employee_id, recorded_at DESC)` and `(workshop_id, specialty_id, recorded_at DESC)`.

### 3. Recording Mechanism
- **Choice**: Record history entries directly within command services (`EmployeeRegistrationCommandServiceImpl` and `WorkshopSpecialtyCommandServiceImpl`) within the same database transaction.
- **Rationale**: Ensures atomic consistency between state update and historical entry without distributed transaction or eventual consistency lag.

### 4. Tenant Security Integration
- Reuse `MultiTenancySecurityService.validateBranchAccess(branchId)` and `validateWorkshopAccess(workshopId)` to guarantee that managers/owners only access historical records within their authorized scope.

## Risks / Trade-offs

- **[Risk] High table growth over time** → Mitigated by lean row sizes and targeted composite indexes on foreign keys plus timestamp.
- **[Risk] Null `changed_by` on automated background tasks** → Fall back to `system` auditor UUID or null when unauthenticated.
