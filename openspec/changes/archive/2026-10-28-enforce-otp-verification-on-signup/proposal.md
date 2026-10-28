# Proposal

## Why

Currently, `POST /api/v1/users` validates email deliverability via EmailVerify.io, but does not enforce that the registrant has proved ownership of the inbox through the One-Time Passcode (OTP) flow (`/api/v1/authentication/otp/send` and `/api/v1/authentication/otp/verify`). This creates a vulnerability where anyone can register an account using any valid third-party email address without the owner's verification or consent.

Enforcing OTP verification upon registration closes this gap, ensuring that accounts are only created after the applicant proves possession of the email account.

## What Changes

- Modify `EmailOtpVerification`: separate `markVerified()` from `markConsumed()` so that verifying the OTP code leaves the verification record in a verified state ready to be consumed by the sign-up process.
- Inject `EmailOtpVerificationRepository` into `UserCommandServiceImpl`.
- In `UserCommandServiceImpl.handle(SignUpCommand)`:
  - Check that an active, verified, unconsumed, and unexpired OTP record exists for the given email address.
  - Reject the registration with `400 Bad Request` (`iam.error.otp.verificationRequired`) if no valid verified OTP record is present.
  - Upon successful user persistence, mark the OTP record as consumed (`markConsumed()`) and save it to ensure single-use protection.
- Update `messages.properties` and `messages_es.properties` with the localized message for `iam.error.otp.verificationRequired`.
- Add/update unit tests in `UserCommandServiceImplTest` and `OtpCommandServiceImplTest`.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `iam/email-otp-verification`: Require and consume a verified OTP record during user account registration.

## Impact

- **IAM Domain**: `EmailOtpVerification.java` (lifecycle separation: verify vs consume)
- **IAM Application Service**: `UserCommandServiceImpl.java` (enforcing OTP verification during signup)
- **i18n**: `messages.properties`, `messages_es.properties` (localized error message)
- **Unit Tests**: `UserCommandServiceImplTest.java`, `OtpCommandServiceImplTest.java`
- **Frontend / Client Impact**: Clients must call `/api/v1/authentication/otp/send` and `/api/v1/authentication/otp/verify` before calling `POST /api/v1/users`.
