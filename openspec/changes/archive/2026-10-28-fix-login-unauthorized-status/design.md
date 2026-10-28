# Design

## Context

See `proposal.md` for motivation. Currently, `AuthenticationController.signIn()` was designed to return `401 UNAUTHORIZED` when `userCommandService.handle(signInCommand)` returns `Optional.empty()`. However, `UserCommandServiceImpl` throws `IllegalArgumentException("iam.error.credentials.invalid")` instead of returning `Optional.empty()`. Because `GlobalExceptionHandler` maps all `IllegalArgumentException` to `400 Bad Request` with `VALIDATION_ERROR`, authentication failures produce HTTP 400 instead of HTTP 401.

## Goals / Non-Goals

**Goals:**
- Ensure `UserCommandServiceImpl.handle(SignInCommand)` and `handle(GoogleSignInCommand)` return `Optional.empty()` when credentials or tokens are invalid, enabling `AuthenticationController` to issue `401 Unauthorized`.
- Map `@PostMapping({"/sessions", "/login"})` on `AuthenticationController` to allow both standard REST resource paths (`/sessions`) and common client expectations (`/login`).
- Maintain existing password hashing and timing behavior to avoid user enumeration or timing leaks.
- Add unit tests verifying both 200 OK and 401 Unauthorized flows for `SignInCommand` and `AuthenticationController`.

**Non-Goals:**
- Modifying token issuance logic (JWT structure, refresh token rotation, or HMAC secrets remain intact).
- Modifying email verification logic for sign-up (`EmailVerifyIoClient` and `BrevoEmailClient` remain untouched).

## Decisions

### Decision 1: Return `Optional.empty()` instead of throwing `IllegalArgumentException`
- **Rationale**: The method signature is already `Optional<AuthenticatedUser> handle(SignInCommand command)`. Returning `Optional.empty()` signals an expected domain condition (authentication failure) without abusing runtime exceptions. The controller already checks `authenticatedUser.isEmpty()` and returns `HttpStatus.UNAUTHORIZED`.
- **Alternative considered**: Catching `IllegalArgumentException` in `AuthenticationController` or adding an exception handler for `BadCredentialsException`. That would violate clean architecture and cause leaky abstractions across boundaries.

### Decision 2: Add `@PostMapping({"/sessions", "/login"})` path alias
- **Rationale**: Frontends and API consumers frequently attempt `POST /api/v1/authentication/login`. Without this mapping, Spring Security permits the URL but DispatcherServlet finds no handler, resulting in a confusing 500 error from the global exception handler. Supporting both paths preserves full backward compatibility with `/sessions` while offering the standard alias `/login`.
- **Alternative considered**: Only documenting `/sessions`. Still risks developer confusion and support friction.

## Risks / Trade-offs

- [Risk]: Clients relying on parsing the 400 `VALIDATION_ERROR` payload on wrong passwords.
  → [Mitigation]: The API documentation in `docs/api/iam/authentication.md` already specifies `401 UNAUTHORIZED`. Aligning with the documented contract fixes broken interceptors and adheres to standard REST practices.
