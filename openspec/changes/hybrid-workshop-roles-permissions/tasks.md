# Tasks

## 1. Permission Catalog & Database Schema

- [x] 1.1 Define the `Permission` enum and `DefaultRolePermissions` mapping in `iam.domain.model.valueobjects` with unit tests verifying every `Roles` enum value maps to its expected default permission set (`./mvnw test -Dtest=PermissionCatalogTest`).
- [ ] 1.2 Create Flyway migration `V21__create_workshop_roles_and_permissions.sql` adding `workshop_roles`, `workshop_role_permissions`, `workshop_role_audit_logs`, nullable `users.workshop_role_id`, and seeding immutable system role templates (`ROLE_OWNER`, `ROLE_BRANCH_MANAGER`, `ROLE_ASSISTANT`, `ROLE_EMPLOYEE`, `ROLE_WAREHOUSE`, `ROLE_CASHIER`, `ROLE_ACCOUNTANT`); verify SQL syntax and schema alignment with persistence entities (`./mvnw test -Dtest=WorkshopRolePersistenceTest`).

## 2. IAM Domain, Persistence & Authority Loading

- [ ] 2.1 Implement `WorkshopRole` aggregate, `WorkshopRoleAuditLog` entity, JPA persistence entities/assemblers/repositories, and `User` `workshopRoleId` support; verify repository/domain unit tests pass (`./mvnw test -Dtest=WorkshopRoleTest`).
- [ ] 2.2 Update `UserDetailsImpl` and `AuthenticatedPrincipal` to expose effective `Permission` authorities (from `workshop_role_permissions` when `workshop_role_id` is present, or `DefaultRolePermissions` fallback otherwise) and verify with `UserDetailsImplTest` (`./mvnw test -Dtest=UserDetailsImplTest`).

## 3. Tenant-Scoped Permission Evaluation in Security Service

- [ ] 3.1 Add `hasPermissionForBranch`, `validateBranchPermission`, `hasPermissionForWorkshop`, and `validateWorkshopPermission` to `MultiTenancySecurityService` (preserving `PLATFORM_ADMIN` bypass and backward-compatible `Roles` fallback for legacy test mocks) and add unit tests in `MultiTenancySecurityServiceTest` (`./mvnw test -Dtest=MultiTenancySecurityServiceTest`).

## 4. Workshop Role Management API, Anti-Escalation & Audit

- [ ] 4.1 Implement `WorkshopRoleCommandService`, `WorkshopRoleQueryService`, and `WorkshopRoleAuditService` enforcing workshop isolation, system-role immutability, anti-privilege-escalation (subset check + `PLATFORM_ADMIN` exclusion), and audit log persistence; verify with service unit tests (`./mvnw test -Dtest=WorkshopRoleCommandServiceTest`).
- [ ] 4.2 Expose `/api/v1/permissions` (`PermissionsController`) and `/api/v1/workshops/{workshopId}/roles` (`WorkshopRolesController` for list, create, update, deactivate, and assign role to staff user), plus controller tests verifying 201/200/204 and 403 anti-escalation/cross-tenant denials (`./mvnw test -Dtest=WorkshopRolesControllerTest,PermissionsControllerTest`).

## 5. Controller Migration & Integration Verification

- [ ] 5.1 Migrate Core, Billing, Fleet, Inventory, Operations, and Media `@PreAuthorize` / `MultiTenancySecurityService` guards to `Permission`-based checks while preserving `ROLE_USER` customer self-service rules; update affected controller tests and expand `EndpointRbacSecurityTest` (`./mvnw test -Dtest=EndpointRbacSecurityTest`).
- [ ] 5.2 Run the full test suite (`./mvnw test`) and update API documentation for `/api/v1/permissions` and `/api/v1/workshops/{workshopId}/roles` (`./mvnw test`).
