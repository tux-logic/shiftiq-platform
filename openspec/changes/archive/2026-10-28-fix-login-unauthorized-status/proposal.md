# Proposal

## Why

When clients submit invalid credentials (wrong email or incorrect password) to the authentication endpoint, the API currently throws an `IllegalArgumentException` in `UserCommandServiceImpl`, which `GlobalExceptionHandler` converts into an HTTP `400 Bad Request` with `VALIDATION_ERROR`. This violates REST conventions, contradicts the API contract documented in `docs/api/iam/authentication.md`, and breaks frontend HTTP interceptors (such as Axios and Flutter/Retrofit) that rely on `401 Unauthorized` to detect authentication failures and guide users to re-authenticate.

Additionally, frontend clients attempting common REST alias paths (e.g. `POST /api/v1/authentication/login`) encounter an unmapped route causing an internal `500 UNEXPECTED_ERROR` instead of being handled gracefully.

## What Changes

- Modify `UserCommandServiceImpl.handle(SignInCommand)` to return `Optional.empty()` instead of throwing `IllegalArgumentException` when the user does not exist or the password does not match.
- Modify `UserCommandServiceImpl.handle(GoogleSignInCommand)` to return `Optional.empty()` instead of throwing `IllegalArgumentException` when Google ID token validation fails.
- Align `AuthenticationController.signIn` to ensure `401 Unauthorized` is returned cleanly when `handle(SignInCommand)` yields `Optional.empty()`.
- Add alias path mapping `@PostMapping({"/sessions", "/login"})` in `AuthenticationController` so that both `/sessions` and `/login` map seamlessly to sign-in.
- Add comprehensive unit tests in `UserCommandServiceImplTest` and `AuthenticationControllerTest` covering successful and failed sign-in scenarios.

## Capabilities

### New Capabilities
- `iam/user-authentication`: Authentication credentials verification, session issuance, and standardized HTTP response handling (200 OK for valid credentials, 401 Unauthorized for invalid credentials, 400 Bad Request for malformed payloads).

### Modified Capabilities
<!-- None -->

## Impact

- **IAM Application Service**: `UserCommandServiceImpl.java`
- **IAM Interfaces**: `AuthenticationController.java`
- **Unit Tests**: `UserCommandServiceImplTest.java`, `AuthenticationControllerTest.java`
- **Clients**: Frontend clients and mobile apps will receive clean, semantic `401 Unauthorized` on bad credentials and can call `/login` or `/sessions`.
