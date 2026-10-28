# Spec Delta

## ADDED Requirements

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
