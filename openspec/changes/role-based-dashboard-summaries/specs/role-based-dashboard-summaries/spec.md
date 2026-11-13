# Specification: Role-Based Dashboard Summaries & Analytics Permission Guard

## ADDED Requirements

### Requirement: Branch analytics endpoints enforce granular ANALYTICS_READ permission
The `Permission` enum SHALL include `ANALYTICS_READ` (`assignableByWorkshop = true`), granted by default to `ROLE_ADMIN`, `ROLE_OWNER`, `ROLE_BRANCH_MANAGER`, and `ROLE_ASSISTANT`, and excluded from `ROLE_EMPLOYEE` and `ROLE_USER`.
`GET /api/v1/analytics/branches/{branchId}/summary` and `GET /api/v1/analytics/branches/{branchId}/financial` SHALL require `ANALYTICS_READ` for the target branch via `MultiTenancySecurityService.validateBranchPermission(branchId, Permission.ANALYTICS_READ)`.

#### Scenario: Mechanic without ANALYTICS_READ is denied branch financial summary
- **WHEN** an authenticated `ROLE_EMPLOYEE` assigned to branch `B` (without a custom role granting `ANALYTICS_READ`) requests `GET /api/v1/analytics/branches/{B}/summary` or `GET /api/v1/analytics/branches/{B}/financial`
- **THEN** the backend responds with HTTP `403` (`ACCESS_DENIED`)

#### Scenario: Branch Manager or Assistant with ANALYTICS_READ can read branch summary
- **WHEN** an authenticated `ROLE_BRANCH_MANAGER` or `ROLE_ASSISTANT` authorized for branch `B` requests `GET /api/v1/analytics/branches/{B}/summary`
- **THEN** the backend responds with HTTP `200` and the branch summary payload

### Requirement: Customer self-service dashboard summary endpoint
`GET /api/v1/analytics/me/customer-summary` SHALL return a lightweight dashboard summary scoped strictly to the authenticated user's `Customer` profile, aggregated via `CustomerDashboardPort`.

#### Scenario: Authenticated customer retrieves personal dashboard summary
- **WHEN** an authenticated `ROLE_USER` with a linked `Customer` profile requests `GET /api/v1/analytics/me/customer-summary`
- **THEN** the backend responds with HTTP `200` containing `customerId`, `activeVehiclesCount`, `activeDtcAlertsCount`, `upcomingAppointmentsCount`, `activeWorkOrdersCount`, `pendingQuotesCount`, and lightweight preview items for upcoming appointments, active work orders, and pending quotes

#### Scenario: Authenticated user without a Customer profile receives empty/zeroed or 404 handling cleanly
- **WHEN** an authenticated user with no `Customer` record requests `GET /api/v1/analytics/me/customer-summary`
- **THEN** the backend responds with HTTP `404` (`CUSTOMER_NOT_FOUND`)

### Requirement: Employee self-service dashboard summary endpoint
`GET /api/v1/analytics/me/employee-summary` SHALL return a lightweight dashboard summary scoped strictly to the authenticated user's `Employee` profile and active branch registrations, aggregated via `EmployeeDashboardPort`.

#### Scenario: Authenticated employee retrieves personal work queue summary
- **WHEN** an authenticated staff member (`ROLE_EMPLOYEE`, `ROLE_ASSISTANT`, `ROLE_BRANCH_MANAGER`, or `ROLE_OWNER`) with a linked `Employee` profile requests `GET /api/v1/analytics/me/employee-summary`
- **THEN** the backend responds with HTTP `200` containing `employeeId`, `assignedWorkOrdersCount`, `pendingTasksCount`, `completedTasksCount`, `activeDtcAlertsCount`, and lightweight preview items for assigned active work orders

#### Scenario: Customer without an Employee profile is denied employee summary
- **WHEN** an authenticated `ROLE_USER` (customer) requests `GET /api/v1/analytics/me/employee-summary`
- **THEN** the backend responds with HTTP `403` (`ACCESS_DENIED`)
