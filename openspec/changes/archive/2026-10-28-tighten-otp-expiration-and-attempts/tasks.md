# Implementation Tasks

## 1. Configuration & Service Defaults
- [x] 1.1 Update `iam.otp.expiration.minutes` default to 5 and `iam.otp.max.attempts` default to 3 in `OtpCommandServiceImpl.java` <!-- id: 1.1 -->
- [x] 1.2 Declare explicit OTP lifecycle properties in `application.properties` and `application.prod.properties` <!-- id: 1.2 -->

## 2. Unit Testing & Verification
- [x] 2.1 Update test fixtures and expectations in `OtpCommandServiceImplTest.java` for 5-minute TTL and 3 attempts <!-- id: 2.1 -->
- [x] 2.2 Run complete test suite with `./mvnw test` to ensure clean build <!-- id: 2.2 -->
