# Tasks

## 1. Configuration & Factiliza Outbound Client

- [x] 1.1 Add Factiliza configuration properties (`factiliza.api.token`, `factiliza.api.url`) to `application.properties`, `application.prod.properties`, and `.env.template`.
- [x] 1.2 Define `IdentityValidationService` outbound port, `PersonIdentityResult`, and `CompanyIdentityResult` in `com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity`.
- [x] 1.3 Implement `FactilizaClient` adapter in `com.tuxlogic.shiftiq.platform.shared.infrastructure.outbound.factiliza` using Spring `RestClient` to query DNI, RUC, and CE endpoints.
- [x] 1.4 Add unit tests in `FactilizaClientTest` verifying DNI, RUC, and CE responses, error codes, and timeout scenarios with `MockRestServiceServer`.

## 2. Lookup REST API & Internationalization

- [x] 2.1 Add localized messages (`shared.error.identity.*`) to `messages.properties` and `messages_es.properties`.
- [x] 2.2 Create `IdentityValidationsController` exposing `GET /api/v1/identity-validations/lookup` with request validation and `IdentityLookupResource` assembler.
- [ ] 2.3 Add unit tests in `IdentityValidationsControllerTest` verifying lookup responses for DNI, RUC, CE, and invalid format handling.

## 3. Profile Creation Validation & Integration

- [ ] 3.1 Integrate `IdentityValidationService` into `CustomerCommandServiceImpl` to validate natural (DNI/CE) and corporate (RUC) documents before saving customer profiles.
- [ ] 3.2 Integrate `IdentityValidationService` into `EmployeeCommandServiceImpl` and `OwnerCommandServiceImpl` to validate person documents before saving employee and owner profiles.
- [ ] 3.3 Integrate `IdentityValidationService` into `WorkshopCommandServiceImpl` to validate workshop RUC (`taxId`) before creating workshops.
- [ ] 3.4 Add unit tests covering document validation in profile command services and run the full test suite with `./mvnw test` ensuring zero failures.
