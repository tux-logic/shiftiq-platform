# Tasks

## 1. Configuration & Brevo Outbound Client

- [x] 1.1 Add Brevo configuration properties (`brevo.api.key`, `brevo.api.url`, `brevo.sender.email`, `brevo.sender.name`) to `application.properties`, `application.prod.properties`, and `.env.template`.
- [x] 1.2 Implement `BrevoEmailClient` adapter in `com.tuxlogic.shiftiq.platform.iam.infrastructure.email` using Spring `RestClient` to dispatch transactional emails to Brevo API v3.
- [x] 1.3 Add unit tests in `BrevoEmailClientTest` covering successful dispatch, invalid API key, and server timeout scenarios, verifying with `./mvnw test -Dtest=BrevoEmailClientTest`.

## 2. OTP Domain Model & Email Template

- [x] 2.1 Create `EmailOtpVerification` domain entity and persistence repository tracking hashed OTPs, expiration timestamps, and attempt counts.
- [x] 2.2 Create responsive HTML email template for ShiftIQ OTP verification with branding, inline styling, and security notices.

## 3. Application Services & Integration

- [ ] 3.1 Implement `OtpCommandService` and `OtpCommandServiceImpl` orchestrating `EmailVerificationService` deliverability pre-check, OTP hash generation, and Brevo email dispatch.
- [ ] 3.2 Add i18n error and confirmation messages (`iam.error.otp.invalid`, `iam.error.otp.expired`, `iam.error.otp.sendFailed`) to `messages.properties` and `messages_es.properties`.
- [ ] 3.3 Add unit tests in `OtpCommandServiceImplTest` verifying pre-check rejection, successful generation/dispatch, and verification validation.

## 4. REST Endpoints & Regression Verification

- [ ] 4.1 Expose `POST /api/v1/authentication/otp/send` and `POST /api/v1/authentication/otp/verify` in `AuthenticationController` with input validation resources and assemblers.
- [ ] 4.2 Add controller unit tests and run the entire test suite with `./mvnw test` ensuring zero failures and clean compilation.
