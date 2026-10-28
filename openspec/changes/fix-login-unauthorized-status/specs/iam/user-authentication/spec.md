# Spec Delta

## Purpose

Authenticates users and provides session tokens, enforcing HTTP 401 Unauthorized for invalid credentials and supporting standard login paths.

## ADDED Requirements

### Requirement: User Authentication with Credentials
The system SHALL verify user credentials against stored password hashes and issue session tokens for valid credentials, or reject authentication with HTTP 401 Unauthorized when credentials are invalid.

#### Scenario: Successful authentication with valid credentials
- **WHEN** a client submits a POST request to `/api/v1/authentication/sessions` or `/api/v1/authentication/login` with an existing user's valid email and correct password
- **THEN** the system returns HTTP 200 OK with an access token, refresh token, user ID, email, role, and expiration details

#### Scenario: Failed authentication with non-existent email
- **WHEN** a client submits a POST request to `/api/v1/authentication/sessions` or `/api/v1/authentication/login` with an email that is not registered
- **THEN** the system returns HTTP 401 Unauthorized

#### Scenario: Failed authentication with incorrect password
- **WHEN** a client submits a POST request to `/api/v1/authentication/sessions` or `/api/v1/authentication/login` with a valid email but an incorrect password
- **THEN** the system returns HTTP 401 Unauthorized

#### Scenario: Malformed request payload
- **WHEN** a client submits a POST request to `/api/v1/authentication/sessions` or `/api/v1/authentication/login` with missing or empty email or password fields
- **THEN** the system returns HTTP 400 Bad Request with validation details

### Requirement: Google OAuth Authentication
The system SHALL authenticate users via Google Identity Services ID tokens and issue session tokens or return HTTP 401 Unauthorized when the Google ID token is invalid.

#### Scenario: Invalid or unverified Google token
- **WHEN** a client submits a POST request to `/api/v1/authentication/sessions/google` with an invalid or expired Google ID token
- **THEN** the system returns HTTP 401 Unauthorized
