# Design

## Context

See `proposal.md` for background and motivation.

ShiftIQ's `iam` context handles authentication and account lifecycle. Deliverability validation is handled by `EmailVerifyIoClient`. This design introduces Brevo's v3 REST API for transactional email delivery to power an end-to-end One-Time Password (OTP) verification system.

## Goals / Non-Goals

**Goals:**
- Implement a dedicated Brevo transactional email adapter (`BrevoEmailClient`) using Spring `RestClient` to hit `POST https://api.brevo.com/v3/smtp/email`.
- Create a domain entity `EmailOtpVerification` tracking hashed OTPs, expiration timestamps (10 minutes), attempt counters (max 5), and verification status.
- Render a responsive HTML email template featuring ShiftIQ branding, centered 6-digit code, expiration warning, and security notice.
- Protect Brevo credit quota and sender reputation by checking email deliverability with `EmailVerificationService` (EmailVerify.io) before dispatching any OTP.
- Provide clean REST endpoints for OTP dispatch and validation.

**Non-Goals:**
- SMS or WhatsApp OTP delivery.
- Replacing existing password recovery SMTP flow unless specifically requested.

## Decisions

### 1. Brevo REST API v3 vs SMTP Relay
- **Choice**: Integrate directly with `POST https://api.brevo.com/v3/smtp/email` using Spring `RestClient`.
- **Rationale**: REST HTTP calls are faster, return immediate confirmation with `messageId`, do not suffer from SMTP connection negotiation latency, and avoid port 25/587 throttling on hosting platforms (such as Render).
- **Alternatives Considered**: Using Spring Mail `JavaMailSender` pointing to `smtp-relay.brevo.com`: Rejected due to synchronous connection latency and lack of rich REST error payloads.

### 2. OTP Security and Hash Storage
- **Choice**: Store the OTP as a cryptographic SHA-256 hash in `EmailOtpVerification` with a 10-minute expiration and a maximum of 5 attempts.
- **Rationale**: Protects OTP codes against database inspection; limits brute-force guessing against the 6-digit keyspace.

### 3. Integrated Deliverability Filter
- **Choice**: Orchestrate `EmailVerificationService` check inside `OtpCommandServiceImpl` prior to invoking Brevo.
- **Rationale**: Rejects invalid, nonexistent, or disposable domains immediately with HTTP 400, eliminating hard bounces on Brevo and conserving transactional credits.

### 4. HTML Template Delivery
- **Choice**: Embedded responsive HTML template with inline CSS styles and parameters substitution.
- **Rationale**: Zero external template engine dependencies needed, fast rendering, consistent appearance across Gmail, Outlook, and mobile clients.

## Risks / Trade-offs

- **[Brevo API Outage or Delay]** → Mitigation: Enforce 3s connection and 5s read timeouts with error handling returning `iam.error.otp.sendFailed`.
- **[Abuse / Repeated OTP Spam]** → Mitigation: Enforce a 60-second cooldown period between OTP generation requests for the same email address.
- **[Hermetic Testability]** → Mitigation: `BrevoEmailClient` is testable via `MockRestServiceServer` and mockable across application service tests.
