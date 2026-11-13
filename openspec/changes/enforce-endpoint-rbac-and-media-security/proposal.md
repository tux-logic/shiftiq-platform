# Proposal

## Why

While tenant-level scoping prevents cross-branch leakage, internal branch endpoints currently rely only on branch membership rather than role boundaries, allowing operational staff (such as mechanics or assistants) to perform destructive management actions like deleting inventory products, altering stock batches, or removing work orders. Additionally, the media upload endpoint accepts file submissions without role filtering or folder destination validation. Enforcing explicit Role-Based Access Control (RBAC) and media upload restrictions closes horizontal privilege escalation gaps while maintaining clean multi-tenant isolation.

## What Changes

- Enforce role-based guards on destructive and management operations in `ProductsController` (deleting products, creating/updating product stock batches require `ROLE_OWNER` or `ROLE_BRANCH_MANAGER`).
- Enforce role-based guards on management operations in `WorkOrdersController` (deleting work orders requires `ROLE_OWNER` or `ROLE_BRANCH_MANAGER`).
- Enforce role-based guards on catalog management in `ServicesController` (creating, updating, and deleting branch services require `ROLE_OWNER` or `ROLE_BRANCH_MANAGER`).
- Restrict `/api/v1/media/upload` so only authenticated staff with authorized roles (`ROLE_OWNER`, `ROLE_BRANCH_MANAGER`, `ROLE_EMPLOYEE`, `ROLE_ADMIN`) can upload to operational asset folders, while whitelisting allowed destination folders and enforcing role restrictions for customer avatars vs. workshop assets.
- Provide comprehensive unit and slice security tests for each secured endpoint verifying HTTP 403 Forbidden for unauthorized roles and HTTP 200/201/204 for authorized roles.

## Capabilities

### New Capabilities
- `iam/role-based-access-control`: Enforces role-based permissions on administrative, operational, and media endpoints in addition to tenant scoping, preventing privilege escalation within branch boundaries.

### Modified Capabilities
<!-- None -->

## Impact

- **Affected Controllers**: `ProductsController`, `WorkOrdersController`, `ServicesController`, `MediaController`.
- **Security & Authorization**: Spring Security Method Security (`@PreAuthorize`) SpEL expressions updated to combine `hasAnyRole(...)` with `@multiTenancySecurityService.isAuthorizedForBranch(...)`.
- **API Clients**: Mobile and web clients invoking administrative actions must present credentials for users holding `ROLE_OWNER`, `ROLE_BRANCH_MANAGER`, or `ROLE_ADMIN`. Read-only or mechanic-specific tasks (e.g., adding parts to assigned work orders) remain accessible to staff.
