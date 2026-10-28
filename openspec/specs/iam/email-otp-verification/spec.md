# iam/email-otp-verification Specification

## Purpose
Provides One-Time Passcode (OTP) generation, delivery through Brevo Transactional Email REST API, and validation to ensure that users verify access to their email inbox during registration and verification flows.

## Requirements

### Requirement: OTP Request and Dispatch via Brevo
The system SHALL generate a secure 6-digit numeric OTP and send it via the Brevo transactional email service after validating deliverability with EmailVerify.io.

#### Scenario: Successful OTP dispatch for deliverable email
- **WHEN** a client requests an OTP for a deliverable, unblocked email address
- **THEN** the system generates an OTP, dispatches the email via Brevo, and returns HTTP 200 OK

#### Scenario: OTP request rejected for invalid email
- **WHEN** a client requests an OTP for an email address that EmailVerify.io marks as invalid or undeliverable
- **THEN** the system rejects the request and returns HTTP 400 Bad Request without calling Brevo

#### Scenario: Rate limiting on OTP dispatch
- **WHEN** a client requests multiple OTPs for the same email address within the cooldown window
- **THEN** the system throttles the request and returns HTTP 429 Too Many Requests or HTTP 400 Bad Request

### Requirement: OTP Code Verification
The system SHALL verify the submitted OTP code against the active, unexpired OTP issued for that email address.

#### Scenario: Valid matching OTP code
- **WHEN** a user submits the correct 6-digit code within the active validity window
- **THEN** the system validates the OTP, marks it as consumed, and returns HTTP 200 OK

#### Scenario: Incorrect OTP code
- **WHEN** a user submits an incorrect code
- **THEN** the system increments the failed attempts counter and returns HTTP 400 Bad Request

#### Scenario: Expired OTP code
- **WHEN** a user submits a code after the expiration period has elapsed
- **THEN** the system rejects the verification and returns HTTP 400 Bad Request

#### Scenario: Max attempts exceeded
- **WHEN** a user exceeds the maximum allowed failed verification attempts
- **THEN** the system invalidates the OTP and returns HTTP 400 Bad Request

### Requirement: Enforce OTP Verification on Registration
The system SHALL require that an active, verified, and unconsumed OTP record exists for the registration email address before allowing new user account creation, and SHALL mark that OTP record as consumed upon successful account creation.

#### Scenario: Registration succeeds with verified unconsumed OTP
- **WHEN** a client submits a registration request with an email address having an active, unconsumed verified OTP
- **THEN** the system creates the user account, marks the OTP record as consumed, and returns HTTP 201 Created

#### Scenario: Registration rejected when no verified OTP exists
- **WHEN** a client submits a registration request with an email address that has not been verified via OTP
- **THEN** the system rejects the registration and returns HTTP 400 Bad Request

#### Scenario: Registration rejected when OTP was already consumed
- **WHEN** a client submits a registration request with an email address whose OTP was already consumed by a prior registration
- **THEN** the system rejects the registration and returns HTTP 400 Bad Request
