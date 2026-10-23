# Spec Delta

## Purpose

Provides real-time deliverability and mailbox authenticity verification for email addresses using the EmailVerify.io external service to ensure only valid, non-disposable emails are registered or updated.

## ADDED Requirements

### Requirement: Real-time Email Verification on User Registration
The system SHALL verify the deliverability and validity of the recipient email address with the external verification provider before creating a new user account.

#### Scenario: Deliverable email allows registration
- **WHEN** a client submits a registration request with an email address verified as `valid`
- **THEN** the system persists the user account and returns HTTP 201 Created

#### Scenario: Undeliverable or invalid email blocks registration
- **WHEN** a client submits a registration request with an email address marked as `invalid` or `do-not-mail`
- **THEN** the system rejects the registration and returns HTTP 400 Bad Request with a localized error code

#### Scenario: Disposable or risky email blocks registration
- **WHEN** a client submits a registration request with a temporary, disposable, or catch-all mailbox rejected by policy
- **THEN** the system rejects the registration and returns HTTP 400 Bad Request

### Requirement: Real-time Email Verification on Email Update
The system SHALL verify the deliverability of the new email address before modifying an existing user's email credential.

#### Scenario: Deliverable new email allows update
- **WHEN** an authenticated user updates their email to an address verified as `valid`
- **THEN** the system updates the user's email and issues a new authentication token

#### Scenario: Undeliverable new email blocks update
- **WHEN** an authenticated user attempts to update their email to an invalid or unreachable address
- **THEN** the system rejects the request and returns HTTP 400 Bad Request

### Requirement: Verification Service Failure Handling
The system SHALL handle timeouts and communication errors with the external verification provider cleanly without exposing internal details to the client.

#### Scenario: External provider is unreachable or times out
- **WHEN** a call to the email verification service times out or fails to return a valid response
- **THEN** the system logs the incident on the server and rejects the request with a localized error message indicating verification failure
