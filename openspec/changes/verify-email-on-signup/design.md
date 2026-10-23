# Design

## Context

See `proposal.md` for background and motivation. 

The `iam` context in ShiftIQ adheres to DDD Hexagonal Architecture, with application ports defined in `application.internal.outboundservices` and implementations in `infrastructure`. Existing patterns (such as `EmailService` with `SmtpEmailService` and Factos gateway in `billing`) provide proven blueprints for integrating external third-party APIs.

## Goals / Non-Goals

**Goals:**
- Provide real-time email deliverability verification before user persistence in `UserCommandServiceImpl.handle(SignUpCommand)` and `handle(UpdateUserEmailCommand)`.
- Encapsulate third-party HTTP interaction behind a clean outbound port interface (`EmailVerificationService`) and infrastructure adapter (`EmailVerifyIoClient`).
- Support configurable API key and endpoint URL via `application.properties` and environment variables (`EMAILVERIFY_API_KEY`, `EMAILVERIFY_API_URL`).
- Provide fast, timeout-bounded HTTP requests (3-5s) using Spring `RestClient`.
- Ensure offline hermetic testability by allowing `EmailVerificationService` to be mocked across unit and integration test suites.

**Non-Goals:**
- Bulk verification of pre-existing accounts in the database.
- Modifying Google OAuth2 Sign-In (`google-sign-in`), as Google accounts are already verified by Google's identity provider.
- Implementing asynchronous webhook callbacks (EmailVerify.io single validation is a synchronous GET request).

## Decisions

### 1. Hexagonal Port and Adapter in IAM
- **Choice**: Define `EmailVerificationService` in `iam.application.internal.outboundservices.email` and implement `EmailVerifyIoClient` in `iam.infrastructure.email`.
- **Rationale**: Isolates the domain and command services from external HTTP libraries, request formats, and vendor-specific response payloads.
- **Alternatives Considered**: 
  - Direct HTTP calls in `UserCommandServiceImpl`: Rejected because it couples domain workflows directly to external infrastructure.
  - Custom Bean Validation annotation (`@ValidEmailAddress`): Rejected because it hides remote I/O and network failure modes inside annotation processors.

### 2. HTTP Client: Spring `RestClient`
- **Choice**: Use Spring Boot 4's native `RestClient` with configured connection and read timeouts.
- **Rationale**: Lightweight, non-blocking fluent API built into Spring Web, requiring zero additional dependencies.
- **Alternatives Considered**:
  - Third-party SDK or OkHttp: Rejected to avoid unnecessary dependency overhead and maintenance.

### 3. Verification Acceptance Policy
- **Choice**: Accept only responses where `status.equalsIgnoreCase("valid")`. Any response with `invalid`, `do-not-mail`, `disposable`, `catch-all`, or error messages will reject the sign-up with an `IllegalArgumentException` mapped to HTTP 400 Bad Request.
- **Rationale**: Fail-closed stance ensures that only actively deliverable mailboxes can be registered in the platform.

### 4. Hermetic Testing Strategy
- **Choice**: In test configuration and unit tests, mock `EmailVerificationService` to verify behavior without making actual network requests to EmailVerify.io.
- **Rationale**: Keeps CI/CD builds fast, deterministic, and runnable in sandboxed environments without requiring live network access or consuming API credits.

## Risks / Trade-offs

- **[Provider Latency or Downtime]** → Mitigation: Enforce a 3-second connection/read timeout on `RestClient`. If the service is unreachable or times out, fail gracefully with a localized error message (`iam.error.email.unverifiable`).
- **[Exhaustion of API Credits]** → Mitigation: Detect error responses such as `"0 credits remaining"` and log a prominent error in server logs while returning a structured error response.
- **[Third-party API Changes]** → Mitigation: Encapsulate JSON parsing in an internal DTO (`EmailVerifyResponse`) within the infrastructure adapter so changes only impact that single class.
