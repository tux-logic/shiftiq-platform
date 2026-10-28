# Tasks

## 1. Branch Setup (Gitflow)

- [x] 1.1 Create and checkout feature branch `feature/enforce-otp-verification-on-signup` from `develop` and verify clean working tree

## 2. Domain Entity and Localization

- [x] 2.1 Refactor `EmailOtpVerification.java` to separate `markVerified()` from `markConsumed()`, add `isReadyForConsumption()` helper, and add `iam.error.otp.verificationRequired` to `messages.properties` and `messages_es.properties`

## 3. Application Service Implementation

- [ ] 3.1 Inject `EmailOtpVerificationRepository` into `UserCommandServiceImpl.java`, enforce verified unconsumed OTP check in `handle(SignUpCommand)`, and consume the OTP record upon account creation

## 4. Unit Testing and Verification

- [ ] 4.1 Update `UserCommandServiceImplTest.java` and `OtpCommandServiceImplTest.java` with test cases for successful OTP consumption, missing OTP, unverified OTP, and already consumed OTP
- [ ] 4.2 Run `./mvnw clean test` to confirm compilation and 100% test pass rate across the platform suite

## 5. API Documentation

- [ ] 5.1 Update `docs/api/iam/users.md` and `docs/api/iam/authentication.md` to document the enforced OTP verification prerequisite before signup
