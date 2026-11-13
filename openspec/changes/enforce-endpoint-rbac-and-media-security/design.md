# Design

## Context

The backend uses Spring Security 6 with `@EnableMethodSecurity`. Multi-tenancy is enforced using `MultiTenancySecurityService.isAuthorizedForBranch(UUID branchId)`, which verifies whether the authenticated principal is assigned to the specified branch or holds global tenant privileges (`ROLE_ADMIN` / `ROLE_OWNER`). 

However, within an authorized branch, destructive and catalog-modifying operations (such as deleting inventory items, altering stock batches, deleting work orders, and creating/updating/deleting workshop services) currently do not restrict execution to managerial roles. Operational staff (`ROLE_EMPLOYEE` or `ROLE_ASSISTANT`) who belong to the branch are therefore able to execute these endpoints. In addition, `MediaController.uploadMedia` allows any authenticated caller to upload files to arbitrary folder paths without role scoping or whitelist validation.

## Goals / Non-Goals

**Goals:**
- Provide a dedicated authorization helper method in `MultiTenancySecurityService` (`isAuthorizedManagerForBranch(UUID branchId)`) verifying that the caller has branch access AND holds `ROLE_ADMIN`, `ROLE_OWNER`, or `ROLE_BRANCH_MANAGER`.
- Enforce managerial role authorization on destructive and catalog-modifying endpoints:
  - `DELETE /api/v1/inventory/products/{productId}`
  - `POST /api/v1/inventory/products/{productId}/stock-batches`
  - `DELETE /api/v1/work-orders/{id}`
  - `POST /api/v1/services`
  - `PUT /api/v1/services/{serviceId}`
  - `DELETE /api/v1/services/{serviceId}`
- Enforce folder whitelisting and role restriction in `MediaController`:
  - Validate `folder` against `ALLOWED_FOLDERS` (`customers`, `employees`, `products`, `vehicles`, `workorders`, `uploads`).
  - Restrict operational folders (`products`, `workorders`, `vehicles`, `employees`) to staff roles (`ROLE_ADMIN`, `ROLE_OWNER`, `ROLE_BRANCH_MANAGER`, `ROLE_EMPLOYEE`).
- Add comprehensive test coverage ensuring 403 Forbidden is returned for unauthorized roles and 200/201/204 for authorized managers.

**Non-Goals:**
- Altering mechanic workflow endpoints (such as assigning tasks, adding products to an active task, or updating task progress).
- Modifying database schemas, JPA entities, or domain aggregate command signatures.
- Replacing the Cloudinary storage adapter implementation.

## Decisions

### Decision 1: Dedicated `isAuthorizedManagerForBranch` Helper in `MultiTenancySecurityService`
- **Choice**: Add `isAuthorizedManagerForBranch(UUID branchId)` to `MultiTenancySecurityService` to evaluate both branch tenancy and managerial roles (`ROLE_ADMIN`, `ROLE_OWNER`, `ROLE_BRANCH_MANAGER`).
- **Rationale**: Keeps SpEL annotations clean (e.g., `@PreAuthorize("@multiTenancySecurityService.isAuthorizedManagerForBranch(#resource.branchId())")`) and centralizes role-hierarchy and tenant validation logic in one testable bean.
- **Alternative considered**: Inlining long SpEL expressions like `@PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or (hasAnyRole('OWNER', 'BRANCH_MANAGER') and @multiTenancySecurityService.isAuthorizedForBranch(#branchId)))")`. Rejected due to verbosity, repetition, and increased risk of syntax or logic drift across controllers.

### Decision 2: Entity Branch Resolution before Management Validation
- **Choice**: For endpoints where `branchId` is not in the request body/path (e.g., `DELETE /api/v1/inventory/products/{productId}` or `DELETE /api/v1/work-orders/{id}`), resolve the entity first via the query service and validate `multiTenancySecurityService.validateManagerAccess(branchId)`.
- **Rationale**: Prevents IDOR and ensures tenancy and role validations are applied to the true persisted entity rather than relying on unauthenticated client-provided claims.
- **Alternative considered**: Requiring `branchId` query parameters on DELETE endpoints. Rejected because changing HTTP endpoint signatures would break existing frontend/mobile clients.

### Decision 3: Folder Whitelist and Role Verification in `MediaController`
- **Choice**: Define an immutable set of allowed folders and inspect `SecurityContextHolder.getContext().getAuthentication()` to ensure non-staff users (`ROLE_USER`) can only upload to customer-facing media destinations (e.g., `customers`, `uploads`), while workshop operational folders require staff authorities.
- **Rationale**: Prevents arbitrary folder pollution in cloud storage and secures operational asset storage without breaking customer avatar uploads.
- **Alternative considered**: Creating separate `/api/v1/media/upload/customer` and `/api/v1/media/upload/staff` endpoints. Rejected to maintain backwards compatibility with existing mobile and web upload flows.

## Risks / Trade-offs

- **[Risk] Existing integration tests fail due to mock users having generic roles** → **Mitigation**: Update test fixtures and mock users in affected controller tests to specify `ROLE_BRANCH_MANAGER` or `ROLE_OWNER` where managerial operations are tested.
- **[Risk] Legitimate mechanics blocked from performing their job** → **Mitigation**: Ensure mechanic-specific actions (e.g., adding parts to a task, starting/completing tasks) remain accessible with `ROLE_EMPLOYEE` and branch tenancy.

## Migration Plan

1. Zero database migration or downtime required.
2. Changes are deployed as part of the standard backend service update.
3. Mobile and web clients that already authenticate as branch managers or owners will operate without any API contract modifications.
