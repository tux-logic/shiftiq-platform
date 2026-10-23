# Tasks

## 1. Configuration and External Service Port

- [x] 1.1 Add `emailverify.api.url` and `emailverify.api.key` configurations to `application.properties`, `application.prod.properties`, and `.env.template` and verify property resolution.
- [x] 1.2 Define `EmailVerificationService` outbound port interface and `EmailVerificationResult` record in `com.tuxlogic.shiftiq.platform.iam.application.internal.outboundservices.email`.

## 2. Infrastructure Client Implementation

- [x] 2.1 Implement `EmailVerifyIoClient` adapter in `com.tuxlogic.shiftiq.platform.iam.infrastructure.email` using Spring `RestClient` with connection/read timeouts and payload deserialization.
- [x] 2.2 Add unit tests in `EmailVerifyIoClientTest` testing scenarios for valid email, invalid status, do-not-mail, and API timeout/errors, verifying with `./mvnw test -Dtest=EmailVerifyIoClientTest`.

## 3. Integration into IAM Command Workflows

- [x] 3.1 Inject `EmailVerificationService` into `UserCommandServiceImpl` and enforce email deliverability checks in `handle(SignUpCommand)` and `handle(UpdateUserEmailCommand)`.
- [ ] 3.2 Add i18n error keys and user-friendly localized messages (`iam.error.email.unverifiable`, `iam.error.email.invalidAddress`) to `messages.properties` and `messages_es.properties`.
- [ ] 3.3 Add unit tests in `UserCommandServiceImplTest` verifying that invalid emails are rejected with an exception and valid emails proceed, running `./mvnw test -Dtest=UserCommandServiceImplTest`.

## 4. Regression and Integration Verification

- [ ] 4.1 Verify controller layer integration in `UsersControllerTest` with mocked verification service ensuring HTTP 400 is returned for invalid emails and HTTP 201 for valid emails.
- [ ] 4.2 Execute the full test suite with `./mvnw test` ensuring zero failures, zero errors, and clean compilation across all bounded contexts.
