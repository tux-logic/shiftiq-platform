# Design

## Context

See `proposal.md` for background and motivation.

ShiftIQ manages personal and organization profiles across the `core` context (`Customer`, `Employee`, `Owner`, `Workshop`) and authentication in `iam`. Profile forms currently require manual entry of identity document numbers (`DNI`, `RUC`, `CE`) and names. Factiliza provides a reliable REST API connected to RENIEC, SUNAT, and Migraciones to validate numbers and supply authoritative names, trade names, and fiscal statuses.

## Goals / Non-Goals

**Goals:**
- Implement `FactilizaClient` in `com.tuxlogic.shiftiq.platform.shared.infrastructure.outbound.factiliza` using Spring `RestClient` with Bearer token authentication and configured timeouts (3s connect, 5s read).
- Define domain port `IdentityValidationService` and immutable result records (`PersonIdentityResult`, `CompanyIdentityResult`).
- Expose a public REST lookup endpoint `GET /api/v1/identity-validations/lookup?type={type}&number={number}` enabling frontend/mobile clients to query identity data and auto-fill profile forms in real-time.
- Integrate document validation into profile creation command handlers in `core` (`CustomerCommandServiceImpl`, `EmployeeCommandServiceImpl`, `OwnerCommandServiceImpl`, `WorkshopCommandServiceImpl`).
- Provide fallback and error handling so that invalid or nonexistent documents return localized 400/404 messages, while service timeouts degrade gracefully without crashing the server.

**Non-Goals:**
- Validation of international passports (no public Peruvian registry API).
- Biometric, facial, or fingerprint verification.
- Enforcing electronic billing certification in SUNAT (already handled by Factos).

## Decisions

### 1. Shared Infrastructure Placement
- **Choice**: Place `IdentityValidationService` port in `shared` (`com.tuxlogic.shiftiq.platform.shared.application.internal.outboundservices.identity`) and `FactilizaClient` in `shared.infrastructure.outbound.factiliza`.
- **Rationale**: Document validation is cross-cutting: it is needed by `core` (Customers, Employees, Owners, Workshops), `fleet` (Mechanic invitations), and publicly by registration/lookup endpoints before a user or profile entity even exists.
- **Alternatives Considered**: Duplicating client in `core` context only (rejected because it prevents IAM or Fleet from performing identity checks).

### 2. Spring `RestClient` with Mock Server Testability
- **Choice**: Use Spring Boot 3 `RestClient` configured with `HttpComponentsClientHttpRequestFactory` and timeouts.
- **Rationale**: Consistent with `EmailVerifyIoClient` and `BrevoEmailClient`. Enables unit testing with `MockRestServiceServer` without external network dependency.

### 3. Unified REST Lookup Endpoint
- **Choice**: `GET /api/v1/identity-validations/lookup?type={DNI|RUC|CE}&number={number}`.
- **Rationale**: A single parameterized endpoint allows the client app to pass the selected document type and number from a single form component, returning a polymorphic or standardized `IdentityLookupResource`.

### 4. Role-Specific Auto-fill Mapping
- **Choice**:
  - `DNI` / `CE` (`ROLE_USER` natural, `ROLE_EMPLOYEE`, `ROLE_ASSISTANT`, `ROLE_BRANCH_MANAGER`, `ROLE_OWNER` natural): maps to `firstName` (`nombres`) and `lastName` (`apellido_paterno + " " + apellido_materno`).
  - `RUC` (`ROLE_USER` corporate, `ROLE_OWNER` workshop): maps to `businessName` (`nombre_o_razon_social`), validates `estado == "ACTIVO"` and `condicion == "HABIDO"`.

## Risks / Trade-offs

- **[Factiliza API Rate Limit / Quota Exhaustion]** → Mitigation: Detect HTTP 402/429 from Factiliza and translate into user-friendly message `core.error.identity.serviceUnavailable`.
- **[Network Latency or RENIEC/SUNAT Delays]** → Mitigation: Enforce 3s connection and 5s read timeouts; cache lookups or return 503 if upstream is unreachable.
- **[Inconsistent Capitalization]** → Mitigation: Format all returned names to title case or uppercase standard before mapping to domain value objects.
