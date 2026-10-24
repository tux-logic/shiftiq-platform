# Design

## Context

Current state: Single monolithic `docs/api-endpoints.md` (1400+ lines) with significant discrepancies vs actual implementation. No navigation structure, no per-context organization. Frontend developers must search through entire file.

Constraints:
- Must align with actual controller code (verified via analysis of 25+ controllers)
- Must follow DDD bounded contexts (Core, IAM, Fleet, IoT, Operations, Billing, Inventory, Analytics, Shared)
- Must provide complete request/response examples with realistic data
- Must document important considerations per endpoint (validation, business rules, security)
- Pure documentation change - no code modifications

## Goals / Non-Goals

**Goals:**
- Create per-bounded-context API documentation files under `docs/api/`
- Each file covers one bounded context with all its endpoints
- Every endpoint has: HTTP method, path, auth requirements, request/response examples, error responses, important considerations
- Create an index file for navigation
- Delete the monolithic `api-endpoints.md`
- Keep `auth-and-roles-flow.md` as complementary deep-dive (already well-structured)

**Non-Goals:**
- Modify any application code
- Change OpenAPI/Swagger annotations in code
- Generate docs from code (manual curation for accuracy)
- Document internal/private endpoints not exposed via REST

## Decisions

### 1. Directory Structure: `docs/api/` by Bounded Context

**Decision**: Organize files by DDD bounded context matching the package structure:
```
docs/api/
├── index.md                          # Navigation & quick reference
├── iam/
│   ├── authentication.md             # login, refresh, logout, google, OTP, password recovery
│   └── users.md                      # sign up, get, update email/password
├── core/
│   ├── workshops.md                  # workshops, specialties
│   ├── branches.md                   # branches, subscriptions
│   ├── owners.md                     # owner profiles
│   ├── employees.md                  # employee profiles
│   ├── customers.md                  # customer profiles
│   └── profiles.md                   # profile roles, document lookup
├── fleet/
│   ├── customer-registrations.md
│   ├── employee-registrations.md     # hierarchical onboarding
│   └── appointments.md
├── iot/
│   ├── vehicles.md
│   ├── obd2-devices.md
│   ├── obd2-device-registrations.md
│   └── telemetry-batches.md
├── operations/
│   ├── services.md
│   ├── work-orders.md
│   └── work-order-tasks.md           # tasks, mechanics, products
├── inventory/
│   └── products.md
├── billing/
│   ├── quotes.md
│   ├── vouchers.md
│   ├── checkouts.md
│   └── mercadopago.md
├── analytics/
│   └── analytics.md
└── shared/
    ├── health.md                     # root, health
    ├── media.md                      # upload
    └── identity-validations.md       # RENIEC/SUNAT/Migraciones lookup
```

**Rationale**: Matches code organization (`com.tuxlogic.shiftiq.platform.<context>`), enables frontend teams to find relevant APIs quickly, scales as contexts grow.

**Alternative considered**: Single file with anchors - rejected due to size and navigation difficulty.

### 2. Endpoint Documentation Format

**Decision**: Each endpoint documented with consistent sections:

```markdown
### `METHOD /path` — Short Description

**Security**: Public / Bearer JWT / Specific Role(s)
**Description**: What it does, business context

**Request**:
- Path params, Query params, Headers, Body schema

**Request Example**:
```json
{ ... }
```

**Success Response** (STATUS):
```json
{ ... }
```

**Error Responses**:
- CODE: Description
- CODE: Description

**Important Considerations**:
- Validation rules
- Business logic notes
- Security/authorization details
- Side effects (events, async jobs)
- Frontend integration tips
```

**Rationale**: Consistent format enables quick scanning, copy-paste examples, clear error handling.

### 3. Authorization Documentation

**Decision**: Document exact `@PreAuthorize` expressions and `MultiTenancySecurityService` method calls per endpoint, not just role names.

**Rationale**: The code uses complex SpEL expressions (e.g., `@multiTenancySecurityService.isAuthorizedForBranch(#resource.branchId())`) and hierarchical rules (`canManageStaff`). Simple role lists are misleading.

### 4. Request/Response Examples

**Decision**: Use realistic UUIDs and data matching the domain (Peruvian context: DNI, RUC, soles currency, Lima timezone).

**Rationale**: Frontend developers need copy-pasteable examples that pass validation.

### 5. Important Considerations Section

**Decision**: Mandatory section per endpoint covering:
- Input validation rules (regex, ranges, required fields)
- Business rule enforcement (state transitions, hierarchical checks)
- Multi-tenancy scoping (which branch/workshop access is validated)
- Side effects (domain events, analytics deltas, async jobs)
- Idempotency/safety notes

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| Documentation drift over time | Add CI check: verify documented endpoints exist in controllers (grep @RequestMapping) |
| Incomplete coverage missing endpoints | Cross-reference with controller scan during creation; use todo list per context |
| Inconsistent format across files | Use the template above strictly; review all files before commit |
| Large PR difficult to review | Split into logical commits per bounded context (follows conventional commits) |
| Duplication with Swagger UI | Swagger is for exploration; these docs are for integration guide with business context |

## Migration Plan

1. Create new `docs/api/` directory structure
2. Write documentation files per bounded context (can be done in parallel)
3. Create `docs/api/index.md` navigation
4. Delete `docs/api-endpoints.md`
5. Verify no broken internal links in `auth-and-roles-flow.md` (update if needed)
6. Single commit per context or one squash commit

## Open Questions

- Should we add a "Getting Started" / "Quick Start" guide in index.md? (Can add later)
- Should we generate a Postman collection from these docs? (Future enhancement)
- Keep example-code/ directory? (Unrelated to API docs, leave as-is)