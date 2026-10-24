# Endpoint Documentation Template (Internal Reference)

Use this template for every endpoint in the context files.

---

### `METHOD /api/v1/...` — Short Descriptive Title

**Security**: Public / Bearer JWT / `@PreAuthorize("...")` expression
**Description**: What this endpoint does, business context, when to use it.

#### Request

| Parameter | Type | Required | Location | Description |
|-----------|------|----------|----------|-------------|
| `paramName` | `string`/`UUID`/`integer`/`boolean` | Yes/No | Path / Query / Header / Body | Description, format, constraints |

**Request Body Schema** (if applicable):
```json
{
  "fieldName": "type",
  "requiredField": "string (required, min 3, max 50)",
  "optionalField": "integer (optional, default: 0)",
  "enumField": "string (enum: VALUE_A, VALUE_B)",
  "dateField": "string (ISO 8601, e.g., 2026-10-24T10:30:00Z)"
}
```

**Request Example**:
```json
{
  "fieldName": "example value",
  "requiredField": "realistic example",
  "optionalField": 42
}
```

#### Success Response (HTTP STATUS)

**Response Body**:
```json
{
  "id": "uuid",
  "fieldName": "example value",
  "createdAt": "2026-10-24T10:30:00Z",
  "nestedObject": {
    "nestedField": "value"
  }
}
```

#### Error Responses

| HTTP Code | Error Code | Description |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Input validation failed (details in `details` field) |
| 401 | `UNAUTHORIZED` | Missing/invalid/expired JWT token |
| 403 | `ACCESS_DENIED` | Insufficient permissions (branch/role) |
| 404 | `NOT_FOUND` | Resource not found |
| 409 | `CONFLICT` | Business rule violation (duplicate, state) |
| 422 | `UNPROCESSABLE_CONTENT` | Semantic validation failed |
| 500 | `UNEXPECTED_ERROR` | Server error (logged, no internal details leaked) |

**Error Response Format**:
```json
{
  "code": "ERROR_CODE",
  "message": "User-friendly localized message",
  "details": "Technical details for debugging"
}
```

#### Important Considerations

- **Validation Rules**: Specific regex, ranges, required combinations
- **Business Logic**: State transitions, hierarchical checks, calculations
- **Multi-Tenancy**: Which `MultiTenancySecurityService` method validates access
- **Side Effects**: Domain events published, analytics deltas, async jobs triggered
- **Idempotency**: Whether safe to retry
- **Frontend Tips**: UX guidance, related endpoints, common patterns

---

## Example: Filled Template

### `POST /api/v1/appointments` — Agendar Nueva Cita

**Security**: Bearer JWT + `@PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @multiTenancySecurityService.isAuthorizedForBranch(#resource.branchId()))")`
**Description**: Creates a new appointment for a customer's vehicle at a specific branch. Validates branch access and emits `AppointmentCreatedEvent` for analytics.

#### Request

| Parameter | Type | Required | Location | Description |
|-----------|------|----------|----------|-------------|
| `branchId` | UUID | Yes | Body | Branch where appointment occurs (must have access) |
| `customerId` | UUID | Yes | Body | Customer profile ID |
| `vehicleId` | UUID | Yes | Body | Vehicle ID (must belong to customer) |
| `scheduledStart` | ISO 8601 datetime | Yes | Body | Appointment start (e.g., `2026-10-15T09:00:00`) |
| `scheduledEnd` | ISO 8601 datetime | No | Body | Appointment end (defaults to +1 hour) |
| `notes` | string | No | Body | Optional notes (max 500 chars) |

**Request Example**:
```json
{
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "customerId": "e5f6a7b8-5555-6666-7777-888899990000",
  "vehicleId": "b1a2c3d4-8888-9999-0000-111122223333",
  "scheduledStart": "2026-10-15T09:00:00",
  "scheduledEnd": "2026-10-15T10:00:00",
  "notes": "Mantenimiento preventivo 20,000 km"
}
```

#### Success Response (201 CREATED)

```json
{
  "id": "a7b8c9d0-8888-9999-0000-111122223333",
  "branchId": "c3d4e5f6-3333-4444-5555-666677778888",
  "customerId": "e5f6a7b8-5555-6666-7777-888899990000",
  "vehicleId": "b1a2c3d4-8888-9999-0000-111122223333",
  "scheduledStart": "2026-10-15T09:00:00",
  "scheduledEnd": "2026-10-15T10:00:00",
  "status": "SCHEDULED",
  "notes": "Mantenimiento preventivo 20,000 km"
}
```

#### Error Responses

| HTTP Code | Error Code | Description |
|-----------|------------|-------------|
| 400 | `VALIDATION_ERROR` | Invalid UUID, missing required fields, past date |
| 403 | `ACCESS_DENIED` | No access to `branchId` |
| 404 | `NOT_FOUND` | Customer/Vehicle/Branch not found |
| 409 | `CONFLICT` | Overlapping appointment for same vehicle |

#### Important Considerations

- **Validation**: `scheduledStart` must be future; `vehicleId` must have active registration in `branchId`
- **Business Logic**: Status starts as `SCHEDULED`; emits `AppointmentCreatedEvent` → Analytics delta dispatcher
- **Multi-Tenancy**: `@multiTenancySecurityService.isAuthorizedForBranch(#resource.branchId())` validates caller's branch assignment
- **Side Effects**: Analytics `totalAppointmentsCount` incremented via delta dispatcher (async replay if DB unavailable)
- **Frontend**: Show branch selector filtered by user's accessible branches; validate date/time before submit