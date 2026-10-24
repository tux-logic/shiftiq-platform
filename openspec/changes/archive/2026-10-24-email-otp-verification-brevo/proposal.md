# Proposal

## Why

While EmailVerify.io validates syntax and deliverability to prevent fake or disposable addresses, it cannot confirm that the registrant actually controls the inbox. Integrating a One-Time Password (OTP) verification mechanism powered by Brevo Transactional Email API (`POST https://api.brevo.com/v3/smtp/email`) guarantees proof of mailbox ownership before account registration or activation.

## What Changes

- **Brevo API v3 Outbound Adapter**: Implement `BrevoEmailService` in IAM infrastructure to dispatch transactional emails via `POST https://api.brevo.com/v3/smtp/email` using Spring `RestClient`.
- **OTP Generation & Lifecycle Model**: Create `EmailOtpVerification` entity to generate 6-digit cryptographic OTPs with configurable TTL (e.g., 10 minutes), attempt rate limiting, and hash verification.
- **Branded HTML Email Template**: Design a clean, responsive HTML email template featuring ShiftIQ branding, prominent OTP display, expiration notice, and security warnings.
- **Combined Deliverability Pre-check**: Prior to issuing and sending the OTP via Brevo, verify email deliverability with `EmailVerifyIoClient` to protect Brevo sender reputation and avoid bounces.
- **Authentication Endpoints**:
  - `POST /api/v1/authentication/otp/send`: Validates email with EmailVerify.io, creates an OTP, and sends the email via Brevo.
  - `POST /api/v1/authentication/otp/verify`: Validates the submitted 6-digit code against the active OTP.
- **Configuration & Secrets**: Configure `brevo.api.key`, `brevo.api.url`, `brevo.sender.email`, and `brevo.sender.name` across application properties and `.env.template`.

## Capabilities

### New Capabilities
- `iam/email-otp-verification`: Issuance, delivery through Brevo Transactional Email API, and verification of One-Time Passcodes (OTP) for user email verification.

### Modified Capabilities
<!-- None: New capability -->

## Impact

- **Affected Contexts**: `iam` (outbound services, domain model, application command/query services, and REST authentication controller).
- **APIs**: Exposes new endpoints under `/api/v1/authentication/otp`.
- **Dependencies**: Uses Spring Web `RestClient` and existing JSON/crypto utilities (zero additional third-party dependencies required).
- **Environment**: Requires `BREVO_API_KEY` and verified sender email (`BREVO_SENDER_EMAIL`) in runtime configurations.
