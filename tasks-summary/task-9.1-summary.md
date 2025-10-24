## Sub-task 9.1: Ensure JWT tokens have proper expiration

### What Changed
- Added `expiresAt` to `AuthResponse` and populated it from `JwtTokenProvider.getExpirationDateFromToken` inside `AuthService`.
- Updated `AuthServiceTest` and `AuthControllerTest` to expect the new expiry metadata and verify token lifecycle behaviour.
- Introduced the `mockk.version` Maven property plus Surefire JVM flags so MockK’s inline agent can attach consistently during the test run.
- Marked the Security checklist item for JWT expiration as complete in `plan_sb.md`.

### Files Updated
- `pom.xml`
- `plan_sb.md`
- `src/main/kotlin/dev/themobileapps/mrrsb/dto/response/AuthResponse.kt`
- `src/main/kotlin/dev/themobileapps/mrrsb/service/AuthService.kt`
- `src/test/kotlin/dev/themobileapps/mrrsb/controller/AuthControllerTest.kt`
- `src/test/kotlin/dev/themobileapps/mrrsb/service/AuthServiceTest.kt`
