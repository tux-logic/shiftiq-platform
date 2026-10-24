# Proposal

## Why

Currently, when a workshop updates a technician's specialty or salary in `EmployeeRegistration`, or modifies a specialty definition in `WorkshopSpecialty`, the database overwrites the existing record in-place. While timestamps (`created_at`, `updated_at`) and soft delete flags (`deleted_at`) exist, workshops have no auditable log or chronological timeline of employee career progression, wage adjustments, specialty reassignments, or specialty catalog changes.

This capability is essential for automotive workshops and fleet managers to track staff professional development, audit payroll/salary adjustments over time, understand historic labor costs, and maintain transparent records of specialty catalog modifications.

## What Changes

- Introduce a career and assignment history tracking mechanism for branch staff (`EmployeeRegistrationHistory` / `StaffCareerHistory`):
  - Automatically records an audit snapshot whenever an employee registration is created, updated (salary, specialty), approved, rejected, or deactivated.
  - Stores historical snapshot data: `registrationId`, `employeeId`, `branchId`, `speciality`, `specialityName`, `salary`, `status`, `changeReason`, `changedBy`, and `effectiveAt`.
- Expose REST query endpoints to retrieve the chronological career history of an employee within a branch or across the workshop.
- Introduce an audit revision tracking mechanism for workshop specialties (`WorkshopSpecialtyHistory`):
  - Records a snapshot whenever a specialty is created, updated (name, description), or deactivated.
  - Stores historical data: `specialtyId`, `workshopId`, `name`, `code`, `description`, `active`, `changeType` (CREATED, UPDATED, DEACTIVATED), `changedBy`, and `recordedAt`.
- Expose REST query endpoints to inspect specialty modification history per workshop.

## Capabilities

### New Capabilities

- `fleet/staff-career-history`: Manages and queries historical snapshots of staff branch assignments, including specialty changes, compensation/salary revisions, and employment status transitions.
- `core/specialty-audit-history`: Manages and queries the chronological revision log for workshop specialty catalog definitions.

### Modified Capabilities

None. Existing aggregates (`EmployeeRegistration`, `WorkshopSpecialty`) will continue to operate as current state projections while emitting domain events or triggering immutable history recordings.

## Impact

- **Database**: New tables `employee_registration_history` (or `staff_career_history`) and `workshop_specialty_history` with indexed foreign keys (`employee_id`, `branch_id`, `specialty_id`, `workshop_id`) and timestamps.
- **Fleet context**: Updated `EmployeeRegistrationCommandServiceImpl` to record or publish history entries on creation, update, approval, rejection, and deactivation. New query services and REST endpoints under `/api/v1/branches/{branchId}/employees/{employeeId}/history`.
- **Core context**: Updated `WorkshopSpecialtyCommandServiceImpl` to record history entries on creation, modification, and deactivation. New query services and REST endpoints under `/api/v1/workshops/{workshopId}/specialties/{specialtyId}/history`.
- **Security & Authorization**: Protected endpoints requiring authentication and workshop/branch tenancy authorization via `MultiTenancySecurityService`.
