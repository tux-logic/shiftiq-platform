# Design

## Context

See `proposal.md` for motivation. In the current implementation, `OtpCommandServiceImpl.handle(VerifyOtpCodeCommand)` calls `otpRecord.markVerified()`, which prematurely sets both `isVerified = true` and `isConsumed = true`. Consequently, `UserCommandServiceImpl` cannot verify whether an active OTP verification is pending consumption during user registration.

## Goals / Non-Goals

**Goals:**
- Separate the OTP verification lifecycle into two distinct phases: verification (`isVerified = true`) and consumption (`isConsumed = true`).
- Enforce in `UserCommandServiceImpl.handle(SignUpCommand)` that the applicant email has an active, verified, unconsumed, and unexpired OTP record before creating the account.
- Mark the OTP record as consumed upon user account creation to prevent reuse.
- Provide localized error feedback (`iam.error.otp.verificationRequired`).
- Update unit tests in `UserCommandServiceImplTest` and `OtpCommandServiceImplTest`.

**Non-Goals:**
- Modifying Google OAuth signup (Google verifies email possession through its own OAuth flow).
- Modifying OTP dispatch, cooldown, or Brevo email delivery mechanisms.

## Decisions

### Decision 1: Refactor `EmailOtpVerification` lifecycle methods
- **Details**:
  - `markVerified()` will set `this.isVerified = true` (leaving `isConsumed = false`).
  - `markConsumed()` will set `this.isConsumed = true`.
  - Add helper `isReadyForConsumption()` returning `isVerified && !isConsumed && Instant.now().isBefore(expiresAt)`.
- **Rationale**: Clean domain model representation that models the business lifecycle: an OTP is verified by entering the code, and then consumed when the user account is created.
- **Alternatives considered**: Passing the raw OTP code directly in `SignUpResource`. Rejected because the user has already verified the code in a previous wizard step, so re-sending the code creates unnecessary state and tight coupling.

### Decision 2: Inject `EmailOtpVerificationRepository` into `UserCommandServiceImpl`
- **Details**:
  - `UserCommandServiceImpl` retrieves the latest OTP record using `findTopByEmailOrderByCreatedAtDesc(email)`.
  - If no record exists, or if `!otpRecord.isReadyForConsumption()`, throws `IllegalArgumentException("iam.error.otp.verificationRequired")`.
  - After `userRepository.save(user)`, invokes `otpRecord.markConsumed()` and `otpRepository.save(otpRecord)`.
- **Rationale**: Transactions in `UserCommandServiceImpl` are `@Transactional`, ensuring that user creation and OTP consumption succeed or fail together atomically.

## Risks / Trade-offs

- [Risk]: Existing tests calling `UserCommandServiceImpl.handle(SignUpCommand)` without an OTP record will fail.
  → [Mitigation]: Update `UserCommandServiceImplTest` setUp/tests with mocked `EmailOtpVerificationRepository` providing verified records for success tests, plus dedicated tests for missing, expired, and already-consumed OTP scenarios.
