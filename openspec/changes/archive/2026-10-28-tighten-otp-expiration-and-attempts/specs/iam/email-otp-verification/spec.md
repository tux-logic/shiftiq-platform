# Spec Delta: iam/email-otp-verification

## MODIFIED Requirements

### Requirement: OTP Code Verification
The system SHALL verify the submitted OTP code against the active, unexpired OTP issued for that email address, enforcing a maximum validity window of 5 minutes and a maximum of 3 failed verification attempts before permanent invalidation.

#### Scenario: Valid matching OTP code
- **WHEN** a user submits the correct 6-digit code within the 5-minute validity window
- **THEN** the system validates the OTP, marks it as verified, and returns HTTP 200 OK

#### Scenario: Incorrect OTP code
- **WHEN** a user submits an incorrect code
- **THEN** the system increments the failed attempts counter and returns HTTP 400 Bad Request

#### Scenario: Expired OTP code
- **WHEN** a user submits a code after the 5-minute expiration period has elapsed
- **THEN** the system rejects the verification and returns HTTP 400 Bad Request

#### Scenario: Max attempts exceeded
- **WHEN** a user exceeds 3 failed verification attempts
- **THEN** the system permanently invalidates the OTP and returns HTTP 400 Bad Request
