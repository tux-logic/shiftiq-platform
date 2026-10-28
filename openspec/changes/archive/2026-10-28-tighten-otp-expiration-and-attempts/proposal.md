# Proposal: Tighten OTP Expiration and Verification Attempts

## Why
The default OTP code lifecycle currently configures an expiration window of 10 minutes and allows up to 5 failed verification attempts before invalidating the code. According to industry security standards (OWASP, NIST) for 6-digit numeric passcodes, a 10-minute validity window is unnecessarily wide for email delivery, and 5 failed attempts gives unnecessary leeway for brute-force or guessing attempts. Reducing the expiration window to 5 minutes and restricting failed attempts to 3 significantly tightens the attack surface while preserving a smooth user experience.

## What Changes
- Reduce default OTP validity window from 10 minutes to 5 minutes in `OtpCommandServiceImpl` and application properties.
- Reduce maximum allowed failed verification attempts from 5 to 3 in `OtpCommandServiceImpl` and application properties.
- Update `application.properties` and `application.prod.properties` to explicitly declare `iam.otp.expiration.minutes=5` and `iam.otp.max.attempts=3`.
- Update email HTML template and documentation to reflect the 5-minute expiration period.
- Update unit tests in `OtpCommandServiceImplTest` to verify the new 5-minute TTL and 3-attempt invalidation behavior.

## Capabilities

### Modified Capabilities
- `iam/email-otp-verification`: Adjust OTP code expiration period to 5 minutes and maximum verification attempts to 3.

## Impact
- **Affected Contexts**: `iam` (application command service configuration, email template, and unit tests).
- **Security**: Reduced brute-force vulnerability and shortened replay window for OTP tokens.
- **Backward Compatibility**: Fully compatible; no database schema or API endpoint contract changes.
