# Tasks

## 1. Security Infrastructure and RBAC Enforcement

- [ ] 1.1 Add `isAuthorizedManagerForBranch` and `validateManagerAccess` to `MultiTenancySecurityService` and verify unit tests in `MultiTenancySecurityServiceTest` pass.
- [ ] 1.2 Enforce manager authorization on product deletion and stock batch management in `ProductsController` and verify compilation and existing unit tests pass.
- [ ] 1.3 Enforce manager authorization on work order deletion in `WorkOrdersController` and verify compilation and existing unit tests pass.
- [ ] 1.4 Enforce manager authorization on service creation, update, and deletion in `ServicesController` and verify compilation and existing unit tests pass.
- [ ] 1.5 Enforce folder whitelisting and role restriction in `MediaController` and verify compilation and unit tests in `MediaControllerTest` pass.

## 2. Test Verification and Documentation

- [ ] 2.1 Add security slice and integration tests in `EndpointRbacSecurityTest` asserting HTTP 403 for unauthorized staff roles and HTTP 200/201/204 for authorized managers and verify with `mvn test`.
- [ ] 2.2 Update API documentation in `docs/api/` reflecting RBAC requirements for affected endpoints and verify docs consistency.

## Workflow follow-up

- Archive the change after verification and PR review.
