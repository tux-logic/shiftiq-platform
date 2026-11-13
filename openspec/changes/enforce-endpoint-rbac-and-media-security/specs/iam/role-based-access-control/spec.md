# Spec Delta

## Purpose

Enforces explicit Role-Based Access Control (RBAC) and upload target authorization across administrative and operational endpoints, preventing horizontal privilege escalation within branch boundaries.

## ADDED Requirements

### Requirement: Branch Administrative Operations RBAC
The system SHALL restrict administrative operations (deleting inventory products, adjusting stock batches, deleting work orders, and creating, updating, or deleting catalog services) to callers holding `ROLE_OWNER`, `ROLE_BRANCH_MANAGER`, or `ROLE_ADMIN` for the target branch, rejecting other staff roles with HTTP 403 Forbidden.

#### Scenario: Branch manager deletes inventory product
- **WHEN** an authenticated user holding `ROLE_BRANCH_MANAGER` or `ROLE_OWNER` authorized for the branch submits a DELETE request to `/api/v1/inventory/products/{productId}`
- **THEN** the system returns HTTP 204 No Content

#### Scenario: Operational employee cannot delete inventory product
- **WHEN** an authenticated user holding `ROLE_EMPLOYEE` or `ROLE_ASSISTANT` submits a DELETE request to `/api/v1/inventory/products/{productId}`
- **THEN** the system returns HTTP 403 Forbidden

#### Scenario: Branch manager deletes work order
- **WHEN** an authenticated user holding `ROLE_BRANCH_MANAGER` or `ROLE_OWNER` authorized for the branch submits a DELETE request to `/api/v1/work-orders/{id}`
- **THEN** the system returns HTTP 204 No Content

#### Scenario: Operational employee cannot delete work order
- **WHEN** an authenticated user holding `ROLE_EMPLOYEE` or `ROLE_ASSISTANT` submits a DELETE request to `/api/v1/work-orders/{id}`
- **THEN** the system returns HTTP 403 Forbidden

#### Scenario: Branch manager manages service catalog
- **WHEN** an authenticated user holding `ROLE_OWNER` or `ROLE_BRANCH_MANAGER` authorized for the branch submits a POST, PUT, or DELETE request to `/api/v1/services`
- **THEN** the system executes the command successfully

#### Scenario: Operational employee cannot manage service catalog
- **WHEN** an authenticated user holding `ROLE_EMPLOYEE` or `ROLE_ASSISTANT` submits a POST, PUT, or DELETE request to `/api/v1/services`
- **THEN** the system returns HTTP 403 Forbidden

### Requirement: Media Upload Role and Folder Authorization
The system SHALL restrict media file uploads to authorized folders based on user roles, allowing customer avatar uploads for authenticated users while reserving operational asset folders (`workorders`, `products`, `vehicles`, `employees`) for staff holding `ROLE_OWNER`, `ROLE_BRANCH_MANAGER`, `ROLE_EMPLOYEE`, or `ROLE_ADMIN`, and rejecting unlisted folder destinations or unauthorized roles with HTTP 403 Forbidden or HTTP 400 Bad Request.

#### Scenario: Staff uploads operational work order asset
- **WHEN** an authenticated staff member holding `ROLE_EMPLOYEE`, `ROLE_BRANCH_MANAGER`, `ROLE_OWNER`, or `ROLE_ADMIN` uploads a valid image targeting the `workorders` folder
- **THEN** the system returns HTTP 201 Created with the uploaded asset secure URL

#### Scenario: Customer attempts upload to operational folder
- **WHEN** an authenticated user holding only `ROLE_USER` submits an image upload targeting folder `workorders` or `products`
- **THEN** the system returns HTTP 403 Forbidden

#### Scenario: Upload targeting invalid or disallowed folder
- **WHEN** an authenticated user submits an upload request with a folder not on the allowed whitelist
- **THEN** the system returns HTTP 400 Bad Request
