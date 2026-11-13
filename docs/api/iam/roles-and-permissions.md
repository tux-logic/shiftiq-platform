# IAM — Permissions & Workshop Roles Endpoints

**Base Paths**: `/api/v1/permissions`, `/api/v1/workshops/{workshopId}/roles`  
**Context**: Identity & Access Management (Hybrid RBAC/PBAC)  
**Authentication**: `Authorization: Bearer <access_token>`

---

## Permission Catalog

The backend owns the atomic `Permission` catalog. All workshop-assignable permissions can be queried via `GET /api/v1/permissions`. `PLATFORM_ADMIN` is excluded from the workshop-assignable catalog and cannot be granted by workshop roles.

| Code | Description |
|------|-------------|
| `WORKORDER_READ` | View work orders and diagnostics |
| `WORKORDER_EXECUTE` | Create, update, and execute work order tasks |
| `WORKORDER_DELETE` | Delete work orders |
| `INVENTORY_READ` | View products and stock batches |
| `INVENTORY_MANAGE` | Create, update, delete products and manage stock batches |
| `SERVICE_MANAGE` | Create, update, and delete workshop services |
| `QUOTE_READ` | View quotes |
| `QUOTE_CREATE` | Create and update draft quotes |
| `QUOTE_APPROVE` | Approve quotes |
| `QUOTE_CANCEL` | Cancel quotes |
| `VOUCHER_READ` | View billing vouchers |
| `CHECKOUT_PROCESS` | Generate vouchers, process checkouts, and record payments |
| `APPOINTMENT_MANAGE` | Create, update, and cancel branch appointments |
| `STAFF_READ` | View branch employee registrations |
| `STAFF_MANAGE` | Create, update, approve, reject, and deactivate staff registrations |
| `SPECIALTY_MANAGE` | Create, update, and deactivate workshop specialties |
| `BRANCH_READ` | View branch details |
| `BRANCH_MANAGE` | Create and update branches and manage branch subscriptions |
| `MEDIA_UPLOAD` | Upload media assets for workshop operations |
| `ANALYTICS_READ` | View branch operational and financial analytics |
| `ROLE_MANAGE` | Create, update, deactivate, and assign custom workshop roles |

---

## `GET /api/v1/permissions` — List Assignable Permissions

**Security**: `@PreAuthorize("isAuthenticated()")`  
**Description**: Returns the catalog of atomic permissions that can be assigned to custom workshop roles. Excludes `PLATFORM_ADMIN`.

### Success Response (200 OK)

```json
[
  {
    "code": "WORKORDER_READ",
    "description": "View work orders and diagnostics"
  },
  {
    "code": "WORKORDER_EXECUTE",
    "description": "Create, update, and execute work order tasks"
  }
]
```

### Error Responses

| HTTP Code | Error Code | Description |
|-----------|------------|-------------|
| 401 | `UNAUTHORIZED` | Missing, invalid, or expired JWT |

---

## `GET /api/v1/workshops/{workshopId}/roles` — List Workshop Roles

**Security**: `@PreAuthorize("@multiTenancySecurityService.hasWorkshopPermission(#workshopId, 'ROLE_MANAGE')")`  
**Description**: Lists all active custom roles defined for the specified workshop.

### Request

| Parameter | Type | Required | Location | Description |
|-----------|------|----------|----------|-------------|
| `workshopId` | UUID | Yes | Path | Workshop identifier |

### Success Response (200 OK)

```json
[
  {
    "id": "b1c2d3e4-5678-49ab-cdef-111122223333",
    "workshopId": "a1b2c3d4-0000-4000-8000-000000000001",
    "name": "Lead Mechanic",
    "description": "Diagnostic and quote drafting lead",
    "system": false,
    "active": true,
    "permissions": [
      "WORKORDER_READ",
      "WORKORDER_EXECUTE",
      "QUOTE_CREATE"
    ]
  }
]
```

### Error Responses

| HTTP Code | Error Code | Description |
|-----------|------------|-------------|
| 401 | `UNAUTHORIZED` | Missing, invalid, or expired JWT |
| 403 | `ACCESS_DENIED` | Caller lacks `ROLE_MANAGE` for the workshop |

---

## `POST /api/v1/workshops/{workshopId}/roles` — Create Custom Workshop Role

**Security**: `@PreAuthorize("@multiTenancySecurityService.hasWorkshopPermission(#workshopId, 'ROLE_MANAGE')")`  
**Description**: Creates a new custom role scoped to the workshop. Anti-escalation rules prevent assigning `PLATFORM_ADMIN` or any permission the actor does not hold (unless `ROLE_ADMIN` / `ROLE_OWNER`).

### Request

| Field | Type | Required | Location | Description |
|-------|------|----------|----------|-------------|
| `workshopId` | UUID | Yes | Path | Workshop identifier |
| `name` | string | Yes | Body | Unique role name within the workshop |
| `description` | string | No | Body | Optional role description |
| `permissions` | Set&lt;string&gt; | Yes | Body | Non-empty set of `Permission` codes |

**Request Example**:
```json
{
  "name": "Service Advisor",
  "description": "Manages appointments and draft quotes",
  "permissions": [
    "APPOINTMENT_MANAGE",
    "QUOTE_READ",
    "QUOTE_CREATE"
  ]
}
```

### Success Response (201 CREATED)

```json
{
  "id": "b1c2d3e4-5678-49ab-cdef-111122223333",
  "workshopId": "a1b2c3d4-0000-4000-8000-000000000001",
  "name": "Service Advisor",
  "description": "Manages appointments and draft quotes",
  "system": false,
  "active": true,
  "permissions": [
    "APPOINTMENT_MANAGE",
    "QUOTE_READ",
    "QUOTE_CREATE"
  ]
}
```

### Error Responses

| HTTP Code | Error Code | Description |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Blank name, empty permissions, or unknown permission code |
| 401 | `UNAUTHORIZED` | Missing, invalid, or expired JWT |
| 403 | `ACCESS_DENIED` | Caller lacks `ROLE_MANAGE`, attempted privilege escalation, or included `PLATFORM_ADMIN` |
| 409 | `CONFLICT` | Role name already exists in the workshop |

---

## `PUT /api/v1/workshops/{workshopId}/roles/{roleId}` — Update Custom Workshop Role

**Security**: `@PreAuthorize("@multiTenancySecurityService.hasWorkshopPermission(#workshopId, 'ROLE_MANAGE')")`  
**Description**: Updates an existing custom workshop role's name, description, and permissions. System roles (`system: true`) are immutable and cannot be modified.

### Request

| Field | Type | Required | Location | Description |
|-------|------|----------|----------|-------------|
| `workshopId` | UUID | Yes | Path | Workshop identifier |
| `roleId` | UUID | Yes | Path | Workshop role identifier |
| `name` | string | Yes | Body | Role name |
| `description` | string | No | Body | Role description |
| `permissions` | Set&lt;string&gt; | Yes | Body | Non-empty set of `Permission` codes |

### Success Response (200 OK)

Returns the updated `WorkshopRoleResource`.

### Error Responses

| HTTP Code | Error Code | Description |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Invalid payload or unknown permission code |
| 401 | `UNAUTHORIZED` | Missing, invalid, or expired JWT |
| 403 | `ACCESS_DENIED` | System role is immutable (`system: true`), privilege escalation attempted, or caller lacks `ROLE_MANAGE` |
| 404 | `NOT_FOUND` | Role not found in the workshop |
| 409 | `CONFLICT` | Role name already exists in the workshop |

---

## `DELETE /api/v1/workshops/{workshopId}/roles/{roleId}` — Deactivate Custom Workshop Role

**Security**: `@PreAuthorize("@multiTenancySecurityService.hasWorkshopPermission(#workshopId, 'ROLE_MANAGE')")`  
**Description**: Soft-deactivates a custom workshop role (`active: false`). System roles (`system: true`) cannot be deactivated.

### Success Response (204 NO CONTENT)

Empty body.

### Error Responses

| HTTP Code | Error Code | Description |
|-----------|------------|-------------|
| 401 | `UNAUTHORIZED` | Missing, invalid, or expired JWT |
| 403 | `ACCESS_DENIED` | System role is immutable (`system: true`) or caller lacks `ROLE_MANAGE` |
| 404 | `NOT_FOUND` | Role not found in the workshop |

---

## `POST /api/v1/workshops/{workshopId}/roles/{roleId}/assignments` — Assign Custom Role to User

**Security**: `@PreAuthorize("@multiTenancySecurityService.hasWorkshopPermission(#workshopId, 'ROLE_MANAGE')")`  
**Description**: Assigns an active custom workshop role to a user within the workshop. Records an audit log entry (`ASSIGNED`).

### Request

| Field | Type | Required | Location | Description |
|-------|------|----------|----------|-------------|
| `workshopId` | UUID | Yes | Path | Workshop identifier |
| `roleId` | UUID | Yes | Path | Workshop role identifier |
| `userId` | UUID | Yes | Body | Target user identifier |

**Request Example**:
```json
{
  "userId": "7b8f9e01-1234-4567-89ab-cdef01234567"
}
```

### Success Response (204 NO CONTENT)

Empty body.

### Error Responses

| HTTP Code | Error Code | Description |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Missing `userId` or inactive role |
| 401 | `UNAUTHORIZED` | Missing, invalid, or expired JWT |
| 403 | `ACCESS_DENIED` | Caller lacks `ROLE_MANAGE` or attempted privilege escalation |
| 404 | `NOT_FOUND` | Role or target user not found |

---

## `GET /api/v1/workshops/{workshopId}/roles/audit-logs` — List Workshop Role Audit Logs

**Security**: `@PreAuthorize("@multiTenancySecurityService.hasWorkshopPermission(#workshopId, 'ROLE_MANAGE')")`  
**Description**: Returns the chronological audit trail of custom role creations, updates, deactivations, and user assignments for the workshop.

### Success Response (200 OK)

```json
[
  {
    "id": "c1d2e3f4-1111-2222-3333-444455556666",
    "workshopId": "a1b2c3d4-0000-4000-8000-000000000001",
    "workshopRoleId": "b1c2d3e4-5678-49ab-cdef-111122223333",
    "actorUserId": "99999999-0000-4000-8000-000000000001",
    "targetUserId": "7b8f9e01-1234-4567-89ab-cdef01234567",
    "actionType": "ASSIGNED",
    "roleName": "Service Advisor",
    "permissionsSnapshot": "APPOINTMENT_MANAGE,QUOTE_CREATE,QUOTE_READ",
    "recordedAt": "2026-11-13T15:00:00Z"
  }
]
```

### Error Responses

| HTTP Code | Error Code | Description |
|-----------|------------|-------------|
| 401 | `UNAUTHORIZED` | Missing, invalid, or expired JWT |
| 403 | `ACCESS_DENIED` | Caller lacks `ROLE_MANAGE` for the workshop |
