# Design: Tighten OTP Expiration and Verification Attempts

## Overview
This design updates the default lifespan of One-Time Passcodes (OTP) issued during registration from 10 minutes to 5 minutes, and restricts maximum failed verification attempts from 5 to 3.

## Architectural Changes

### 1. Application Configuration Defaults
In `OtpCommandServiceImpl.java`:
- Change default `@Value("${iam.otp.expiration.minutes:10}")` to `@Value("${iam.otp.expiration.minutes:5}")`.
- Change default `@Value("${iam.otp.max.attempts:5}")` to `@Value("${iam.otp.max.attempts:3}")`.
- Update `src/main/resources/application.properties` and `src/main/resources/application.prod.properties` to declare:
  ```properties
  # IAM OTP Lifecycle Settings
  iam.otp.expiration.minutes=${IAM_OTP_EXPIRATION_MINUTES:5}
  iam.otp.max.attempts=${IAM_OTP_MAX_ATTEMPTS:3}
  iam.otp.resend.cooldown.seconds=${IAM_OTP_COOLDOWN_SECONDS:60}
  ```

### 2. Email Notification Template
In `OtpEmailTemplateBuilder.java`:
- The template already uses the dynamic token `{{EXPIRATION_MINUTES}} minutos`.
- When passed `5`, it will automatically render: `Este código expirará en 5 minutos.`

### 3. Verification Logic
- In `EmailOtpVerification.java`: constructor receives `expirationMinutes` (5) and `maxAttempts` (3), calculating `expiresAt = createdAt.plus(5, ChronoUnit.MINUTES)`.
- When verifying, if `attempts >= maxAttempts` (3), it triggers max attempts exceeded.

### 4. Testing & Regression
- Update `OtpCommandServiceImplTest.java` to test 5-minute expiration and 3 maximum attempts.
- Ensure all IAM unit tests continue to pass with 100% green build.
