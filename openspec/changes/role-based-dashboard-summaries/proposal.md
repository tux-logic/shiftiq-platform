# Proposal: Role-Based Dashboard Summaries & Analytics Permission Guard

## Why

1. **Over-permissive branch analytics access**: `AnalyticsController` currently gates `/api/v1/analytics/branches/{branchId}/summary` and `/api/v1/analytics/branches/{branchId}/financial` using `multiTenancySecurityService.isAuthorizedForBranch(branchId)` without a granular permission check. Consequently, any branch staff member (including `ROLE_EMPLOYEE` mechanics) can read branch financial revenue (`totalRevenue`) and daily financial snapshots.
2. **Missing lightweight dashboard summaries for customers and employees**: The `analytics` bounded context only exposes branch/network executive KPIs. Mobile and web clients building a Customer or Employee dashboard would otherwise need to make 5–8 parallel REST calls across `fleet`, `operations`, `billing`, and `iot`, increasing latency and duplicating aggregation logic.
3. **Preserve modular-monolith boundaries**: Following Option 2 (Aggregator / BFF via read-only ACL ports), `analytics` will aggregate summary data exclusively through dedicated read-only port interfaces (`*Port` / `*PortImpl`) over published application `QueryService` contracts—never touching repositories of other bounded contexts.

## What Changes

1. **Granular `ANALYTICS_READ` permission**:
   - Add `ANALYTICS_READ` to `Permission` (workshop-assignable) and grant it by default to `ROLE_ADMIN`, `ROLE_OWNER`, `ROLE_BRANCH_MANAGER`, and `ROLE_ASSISTANT` (excluded from `ROLE_EMPLOYEE` and `ROLE_USER`).
   - Protect `/api/v1/analytics/branches/{branchId}/summary` and `/api/v1/analytics/branches/{branchId}/financial` with `validateBranchPermission(branchId, Permission.ANALYTICS_READ)` so `ROLE_EMPLOYEE` without `ANALYTICS_READ` receives `403 ACCESS_DENIED`.
2. **Read-only Anti-Corruption Layer (ACL) Ports in `analytics`**:
   - Define `CustomerDashboardPort` and `EmployeeDashboardPort` in `analytics/domain/services/` with clean read-only summary DTOs, implemented in `analytics/infrastructure/acl/` against existing `core`, `fleet`, `operations`, `billing`, and `iot` `QueryService` interfaces.
3. **Role-specific self-service dashboard endpoints**:
   - `GET /api/v1/analytics/me/customer-summary` (`ROLE_USER` / authenticated customer): returns active vehicles count, active DTC alerts count, upcoming appointments, active work orders summary, and pending quotes count.
   - `GET /api/v1/analytics/me/employee-summary` (authenticated workshop staff): returns assigned branch IDs, active assigned work orders/tasks summary, completed work orders count, and active DTC alerts on vehicles in their active work orders.
4. **Tests & API Documentation**:
   - Unit & WebMvc tests covering `ANALYTICS_READ` enforcement, ACL port adapters, and both `/me/*-summary` endpoints, plus updated `docs/api/analytics/analytics.md`.

## Capabilities

### New Capabilities
- `role-based-dashboard-summaries`: Granular `ANALYTICS_READ` guard for branch financial analytics and ACL-backed `customer-summary` / `employee-summary` dashboard endpoints.
