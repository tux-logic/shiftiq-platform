# Implementation Tasks

## 1. IAM Permission & Branch Analytics Guard
- [x] 1.1 Add `ANALYTICS_READ` to `Permission` and `DefaultRolePermissions` (granted to `ADMIN`, `OWNER`, `BRANCH_MANAGER`, `ASSISTANT`; excluded from `EMPLOYEE` and `USER`), and update `PermissionCatalogTest` (`./mvnw test -Dtest=PermissionCatalogTest`).
- [x] 1.2 Enforce `Permission.ANALYTICS_READ` via `MultiTenancySecurityService.validateBranchPermission(branchId, Permission.ANALYTICS_READ)` in `AnalyticsController` for `/branches/{branchId}/summary` and `/branches/{branchId}/financial`; update `AnalyticsControllerTest` and `EndpointRbacSecurityTest` (`./mvnw test -Dtest=AnalyticsControllerTest,EndpointRbacSecurityTest`).

## 2. ACL Dashboard Ports & Query Services
- [x] 2.1 Define `CustomerDashboardPort`, `EmployeeDashboardPort`, and domain summary records/queries in `analytics/domain`, and implement `CustomerDashboardPortImpl` and `EmployeeDashboardPortImpl` in `analytics/infrastructure/acl/` with unit tests (`./mvnw test -Dtest=CustomerDashboardPortImplTest,EmployeeDashboardPortImplTest`).
- [ ] 2.2 Wire `GetCustomerDashboardSummaryQuery` and `GetEmployeeDashboardSummaryQuery` into `AnalyticsQueryService` and `AnalyticsQueryServiceImpl` with unit tests (`./mvnw test -Dtest=AnalyticsQueryServiceImplTest`).

## 3. Self-Service Dashboard Endpoints & Documentation
- [ ] 3.1 Expose `GET /api/v1/analytics/me/customer-summary` and `GET /api/v1/analytics/me/employee-summary` in `AnalyticsController` with response resources and WebMvc tests (`./mvnw test -Dtest=AnalyticsControllerTest,EndpointRbacSecurityTest`).
- [ ] 3.2 Update `docs/api/analytics/analytics.md` for `ANALYTICS_READ` and the new `/me/customer-summary` and `/me/employee-summary` endpoints, and run the full `./mvnw test` suite (`./mvnw test`).
