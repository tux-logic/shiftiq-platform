# Proposal

## Why

Currently, user registration (`POST /api/v1/users`) only validates email syntax via basic regex/Bean Validation, allowing non-existent domains, typos, and disposable emails to be persisted. Integrating real-time validation via the EmailVerify.io external service ensures that only deliverable, authentic email addresses can register, protecting system integrity and preventing fake or unreachable user accounts.

## What Changes

- **External Provider Integration**: Add client integration with EmailVerify.io (`https://app.emailverify.io/api/v1/validate`) utilizing the configured API key (`EMAILVERIFY_API_KEY`).
- **Configuration & Environment**: Configure `emailverify.api.url` and `emailverify.api.key` in `application.properties`, `.env.template`, and local `.env`.
- **Application Port & Outbound Service**: Create the `EmailVerificationService` port in IAM application layer and its infrastructure implementation `EmailVerifyIoClient` using Spring `RestClient`/`RestTemplate`.
- **Registration Validation Interception**: Intercept the sign-up flow in `UserCommandServiceImpl.handle(SignUpCommand)` to verify email validity before saving the new user. If the verification returns `invalid`, `do-not-mail`, or unverifiable status, abort registration and throw an exception mapped to a standard HTTP 400 response.
- **Internationalization (i18n)**: Add localized error messages (`iam.error.email.unverifiable`, `iam.error.email.invalidAddress`) to `messages.properties` and `messages_es.properties`.

## Capabilities

### New Capabilities
- `iam/email-verification`: Real-time deliverability and mailbox verification for email addresses using EmailVerify.io during account registration and email changes.

### Modified Capabilities
<!-- None: This is the initial spec for this capability in the repository -->

## Impact

- **Affected Contexts**: `iam` (`application.internal.commandservices.UserCommandServiceImpl`, new outbound port and adapter in `infrastructure`).
- **APIs**: `POST /api/v1/users` will now return HTTP 400 Bad Request with a standard error body when EmailVerify.io determines the email is invalid or undeliverable.
- **Dependencies**: Uses standard Spring Web `RestClient` (no extra third-party SDK dependencies required).
- **Environment**: Requires `EMAILVERIFY_API_KEY` to be configured in `.env` and runtime environments.
