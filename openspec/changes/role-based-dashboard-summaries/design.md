# Design: Role-Based Dashboard Summaries & Analytics Permission Guard

## Architecture

### 1. Permission Catalog Extension (`iam`)
- Add `Permission.ANALYTICS_READ("View branch operational and financial analytics", true)` to `Permission.java`.
- Grant `ANALYTICS_READ` in `DefaultRolePermissions.java` to `ALL_WORKSHOP_PERMISSIONS` (`ROLE_OWNER`, `ROLE_BRANCH_MANAGER`), `ASSISTANT_PERMISSIONS` (`ROLE_ASSISTANT`), and `ROLE_ADMIN`. Keep it excluded from `EMPLOYEE_PERMISSIONS` (`ROLE_EMPLOYEE`) and `ROLE_USER`.

### 2. Anti-Corruption Layer Ports (`analytics`)
Following the existing `iot` port pattern (`CustomerDirectoryPort` / `CustomerDirectoryPortImpl`):
- `analytics/domain/services/CustomerDashboardPort.java`
  - `Optional<CustomerDashboardSummary> loadSummaryForUser(UUID userId)`
- `analytics/domain/services/EmployeeDashboardPort.java`
  - `Optional<EmployeeDashboardSummary> loadSummaryForUser(UUID userId)`
- Implementations in `analytics/infrastructure/acl/`:
  - `CustomerDashboardPortImpl`: injects `CustomerQueryService`, `VehicleQueryService`, `DtcAlertQueryService`, `AppointmentQueryService`, `WorkOrderQueryService`, `QuoteQueryService`. Resolves `Customer` by `userId`, loads active vehicles, active DTC alerts per vehicle, upcoming appointments (`PENDING` / `CONFIRMED`), active work orders per vehicle, and pending quotes (`DRAFT` / `SENT`).
  - `EmployeeDashboardPortImpl`: injects `EmployeeQueryService`, `EmployeeRegistrationQueryService`, `WorkOrderQueryService`, `DtcAlertQueryService`. Resolves `Employee` by `userId`, resolves active branch registrations, loads work orders across assigned branches, filters work orders/tasks assigned to `employeeId`, computes pending/completed task counts and active DTC alerts on those vehicles.
- No repositories from `core`, `fleet`, `operations`, `billing`, or `iot` are injected into `analytics`.

### 3. Application & REST Layer (`analytics`)
- Extend `AnalyticsQueryService` / `AnalyticsQueryServiceImpl` with:
  - `handle(GetCustomerDashboardSummaryQuery)`
  - `handle(GetEmployeeDashboardSummaryQuery)`
- Update `AnalyticsController`:
  - `GET /api/v1/analytics/branches/{branchId}/summary` & `/financial`: replace `isAuthorizedForBranch(branchId)` check with `multiTenancySecurityService.validateBranchPermission(branchId, Permission.ANALYTICS_READ)`.
  - `GET /api/v1/analytics/me/customer-summary`: `@PreAuthorize("isAuthenticated()")`, resolves current `userId` via `MultiTenancySecurityService`, returns `200 OK` or `404 CUSTOMER_NOT_FOUND`.
  - `GET /api/v1/analytics/me/employee-summary`: `@PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'BRANCH_MANAGER', 'ASSISTANT', 'EMPLOYEE')")`, resolves current `userId`, returns `200 OK` or `404 EMPLOYEE_NOT_FOUND`.
