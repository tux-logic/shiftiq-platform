# ShiftIQ Platform — API Documentation Index

**Version**: 1.0  
**Last Updated**: 2026-10-24  
**Base URL (Local)**: `http://localhost:8080`  
**Base URL (Production)**: `https://shiftiq-platform.onrender.com`  
**Swagger UI**: `https://shiftiq-platform.onrender.com/swagger-ui/index.html`

---

## 📚 Documentation by Bounded Context

| Context | Domain | File | Key Endpoints |
|---------|--------|------|---------------|
| **IAM** | Identity & Access Management | [`iam/authentication.md`](iam/authentication.md) | Login, Refresh, Logout, Google OAuth, OTP, Password Recovery |
| | | [`iam/users.md`](iam/users.md) | Sign Up, Get User, Update Email/Password |
| **Core** | Workshops, Branches, Profiles | [`core/workshops.md`](core/workshops.md) | Workshops, Specialties (CRUD + History) |
| | | [`core/branches.md`](core/branches.md) | Branches, Subscriptions |
| | | [`core/owners.md`](core/owners.md) | Owner Profiles (self-only access) |
| | | [`core/employees.md`](core/employees.md) | Employee Profiles (hierarchical auth) |
| | | [`core/customers.md`](core/customers.md) | Customer Profiles (hierarchical auth) |
| | | [`core/profiles.md`](core/profiles.md) | Profile Roles, Document Lookup |
| **Fleet** | Vehicle & Staff Management | [`fleet/customer-registrations.md`](fleet/customer-registrations.md) | Customer ↔ Branch Registrations |
| | | [`fleet/employee-registrations.md`](fleet/employee-registrations.md) | Hierarchical Staff Onboarding, Approvals |
| | | [`fleet/appointments.md`](fleet/appointments.md) | Workshop Appointments |
| **IoT** | Vehicles, OBD2, Telemetry | [`iot/vehicles.md`](iot/vehicles.md) | Vehicles, Telemetry History, DTC Alerts |
| | | [`iot/obd2-devices.md`](iot/obd2-devices.md) | OBD2 Hardware Devices, Live Telemetry |
| | | [`iot/obd2-device-registrations.md`](iot/obd2-device-registrations.md) | Device ↔ Vehicle Linking |
| | | [`iot/telemetry-batches.md`](iot/telemetry-batches.md) | Batch Telemetry Ingestion |
| **Operations** | Work Orders & Services | [`operations/services.md`](operations/services.md) | Service Catalog |
| | | [`operations/work-orders.md`](operations/work-orders.md) | Work Orders (Lifecycle) |
| | | [`operations/work-order-tasks.md`](operations/work-order-tasks.md) | Tasks, Mechanics, Products (Stock Reservation) |
| **Inventory** | Products & Stock | [`inventory/products.md`](inventory/products.md) | Products, Batches, Stock Adjustments |
| **Billing** | Quotes, Invoicing, Payments | [`billing/quotes.md`](billing/quotes.md) | Quotes (Draft → Approved → Canceled) |
| | | [`billing/vouchers.md`](billing/vouchers.md) | SUNAT Invoices/Receipts (Factos) |
| | | [`billing/checkouts.md`](billing/checkouts.md) | Unified Checkout (Cash + Mercado Pago) |
| | | [`billing/mercadopago.md`](billing/mercadopago.md) | Preferences, Webhooks |
| **Analytics** | KPIs & Dashboards | [`analytics/analytics.md`](analytics/analytics.md) | Branch Summary, Financial Range, Network Overview |
| **Shared** | Cross-Cutting Utilities | [`shared/health.md`](shared/health.md) | Root Ping, Health Check |
| | | [`shared/media.md`](shared/media.md) | Cloudinary Image Upload |
| | | [`shared/identity-validations.md`](shared/identity-validations.md) | RENIEC/SUNAT/Migraciones Lookup |

---

## 🔐 Authentication Quick Reference

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/v1/authentication/sessions` (o `/login`) | POST | Public | Login (email/password) → tokens |
| `/api/v1/authentication/sessions/google` | POST | Public | Google OAuth login → tokens |
| `/api/v1/authentication/sessions/refresh` | POST | Public | Refresh token rotation |
| `/api/v1/authentication/sessions` | DELETE | Public | Logout (revoke refresh token) |

**Token Usage**: Include `Authorization: Bearer <access_token>` in all protected requests.  
**Token Expiry**: Access = 15 min, Refresh = 7 days (single-use rotation).

---

## 🎭 Role Hierarchy (for Authorization)

| Role | Level | Scope |
|------|-------|-------|
| `ROLE_ADMIN` | 4 | Platform-wide (all branches) |
| `ROLE_OWNER` | 3 | Own workshops/branches only |
| `ROLE_BRANCH_MANAGER` | 2 | Assigned branches only |
| `ROLE_ASSISTANT` | 1 | Assigned branches only |
| `ROLE_EMPLOYEE` | 0 | Assigned branches only |
| `ROLE_USER` | 0 | Customer (no branch access) |

**Key Rule**: PUT/DELETE on Owners/Employees/Customers require **self or ADMIN only** (no ROLE_OWNER bypass).

---

## 🚀 Quick Start for Frontend

1. **Login**: `POST /api/v1/authentication/sessions` → save `token` + `refreshToken`
2. **Interceptor**: On 401 → `POST /api/v1/authentication/sessions/refresh` with `refreshToken` → retry
3. **Branch Access**: All branch-scoped endpoints validate `MultiTenancySecurityService.isAuthorizedForBranch(branchId)`
4. **Role Changes**: After role assignment, user must re-login or refresh session for new JWT claims

---

## 📝 Documentation Conventions

Each endpoint file follows this format:

```markdown
### `METHOD /path` — Description

**Security**: Public / Bearer JWT / Role(s)
**Description**: Business context

**Request**: Path/Query/Body params with types

**Request Example**: JSON

**Success Response** (STATUS): JSON

**Error Responses**: CODE - Description

**Important Considerations**: Validation, business rules, side effects
```

---

## 🔗 Related Documents

- [Authentication & Roles Flow](../auth-and-roles-flow.md) — Deep dive on JWT lifecycle, hierarchical onboarding, multi-tenancy

---

## 📧 Support

For API issues, contact the backend team or check Swagger UI for interactive testing.