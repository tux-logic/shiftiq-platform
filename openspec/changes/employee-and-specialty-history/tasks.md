# Tasks

## 1. Database Schema & Persistence Entities

- [x] 1.1 Create Flyway migration `V16__create_staff_and_specialty_history_tables.sql` defining `staff_career_history` and `workshop_specialty_history` with indexed foreign keys and timestamps.
- [x] 1.2 Implement `StaffCareerHistoryPersistenceEntity` and Spring Data JPA `StaffCareerHistoryPersistenceRepository` in `fleet`.
- [x] 1.3 Implement `WorkshopSpecialtyHistoryPersistenceEntity` and Spring Data JPA `WorkshopSpecialtyHistoryPersistenceRepository` in `core`.

## 2. Fleet Staff Career History Implementation

- [x] 2.1 Define `StaffCareerSnapshot` domain model and `StaffCareerHistoryRepository` domain port in `fleet`.
- [x] 2.2 Update `EmployeeRegistrationCommandServiceImpl` to record career snapshots on creation, specialty/salary updates, approvals, rejections, and deactivations.
- [x] 2.3 Implement `StaffCareerHistoryQueryService` and `GetStaffCareerHistoryByEmployeeAndBranchQuery` handler.
- [ ] 2.4 Expose REST endpoint `GET /api/v1/branches/{branchId}/employees/{employeeId}/history` with resource assembler and multi-tenant security validation.
- [ ] 2.5 Add unit tests in `StaffCareerHistoryTest` and `StaffCareerHistoryControllerTest` verifying snapshot recording and query endpoints.

## 3. Core Workshop Specialty History Implementation

- [ ] 3.1 Define `WorkshopSpecialtyRevision` domain model and `WorkshopSpecialtyHistoryRepository` domain port in `core`.
- [ ] 3.2 Update `WorkshopSpecialtyCommandServiceImpl` to record revision snapshots on specialty creation, seeding, updates, and deactivations.
- [ ] 3.3 Implement `WorkshopSpecialtyHistoryQueryService` and `GetWorkshopSpecialtyHistoryQuery` handler.
- [ ] 3.4 Expose REST endpoint `GET /api/v1/workshops/{workshopId}/specialties/{specialtyId}/history` with resource assembler and multi-tenant security validation.
- [ ] 3.5 Add unit tests in `WorkshopSpecialtyHistoryTest` and run the full test suite with `./mvnw test` ensuring zero failures.
