# Tasks

## 1. Branch and Workspace Setup (Gitflow)

- [x] 1.1 Create and checkout feature branch `fix/login-unauthorized-status` from `develop` and verify clean working tree

## 2. Core Implementation

- [x] 2.1 Update `UserCommandServiceImpl.java` so `handle(SignInCommand)` returns `Optional.empty()` on unknown user or mismatched password, and `handle(GoogleSignInCommand)` returns `Optional.empty()` on invalid Google ID token
- [x] 2.2 Update `AuthenticationController.java` to map `@PostMapping({"/sessions", "/login"})` for sign-in and ensure `HttpStatus.UNAUTHORIZED` is returned cleanly when `handle(SignInCommand)` is empty

## 3. Unit Tests and Verification

- [x] 3.1 Add unit tests in `UserCommandServiceImplTest.java` covering successful sign-in, user not found, and wrong password returning `Optional.empty()`
- [x] 3.2 Add unit tests in `AuthenticationControllerTest.java` verifying `POST /api/v1/authentication/sessions` and `POST /api/v1/authentication/login` return 200 on success and 401 on invalid credentials
- [x] 3.3 Run `./mvnw clean test` to ensure all tests pass cleanly (352 tests run, 0 failures, 0 errors)

## 4. Documentation and Delivery

- [x] 4.1 Update API documentation if needed and confirm conventional commits style
- [x] 4.2 Mark tasks completed and prepare delivery report
