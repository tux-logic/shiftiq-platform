# Proposal

## Why

ShiftIQ requires accurate and verifiable personal identity and tax data for users based on their assigned roles (Customers, Employees, Workshop Owners, and Workshops). Currently, document numbers (DNI, RUC, CE) and personal/business names are entered manually without official registry validation, leading to typos, nonexistent documents, and fraudulent profile data. Integrating Factiliza (official RENIEC, SUNAT, and Migraciones Peruvian verification API) enables automated identity document verification and instant auto-filling of official legal names, trade names, and fiscal status.

## What Changes

- Add Factiliza API configuration properties (`factiliza.api.token`, `factiliza.api.url`) to `application.properties`, `application.prod.properties`, and `.env.template`.
- Implement `FactilizaClient` adapter in `com.tuxlogic.shiftiq.platform.shared.infrastructure.outbound.factiliza` using Spring `RestClient` with configured timeouts (3s connect, 5s read) and Bearer authentication to query DNI (`/v1/dni/info/{dni}`), RUC (`/v1/ruc/info/{ruc}`), and CE (`/pe/v1/cee/info/{cee}`).
- Define outbound port `IdentityValidationService` and result models (`PersonValidationResult`, `CompanyValidationResult`).
- Expose REST query endpoints for identity lookup and auto-fill: `GET /api/v1/identity-validations/lookup?type={DNI|RUC|CE}&number={number}`.
- Provide domain verification and auto-completion tailored to ShiftIQ roles:
  - **Natural Persons** (`ROLE_USER` natural, `ROLE_EMPLOYEE`, `ROLE_ASSISTANT`, `ROLE_BRANCH_MANAGER`, `ROLE_OWNER` natural): validates existence and autocompletes `firstName`, `lastName` from RENIEC/Migraciones.
  - **Legal Entities** (`ROLE_USER` corporate, `ROLE_OWNER` workshop): validates existence, condition (`HABIDO`), and status (`ACTIVO`), autocompleting `businessName` (Razón Social) and address details.
- Validate incoming documents in profile creation commands (`Customer`, `Employee`, `Owner`, `Workshop`) against Factiliza before persisting records.

## Capabilities

### New Capabilities
- `identity/document-validation`: Verification of official Peruvian identification documents (DNI, RUC, CE) and auto-completion of identity and fiscal data via Factiliza REST API.

### Modified Capabilities
None.

## Impact
- **New External Integration**: Factiliza API (`https://api.factiliza.com`).
- **New Adapters & Services**: `FactilizaClient`, `IdentityValidationService`.
- **New REST Endpoints**: `GET /api/v1/identity-validations/lookup`.
- **Updated Profile Creation Flows**: Profile command services validate documents with Factiliza.
- **Dependencies**: Uses Spring Web `RestClient` and Jackson (no extra external libraries).
