# E-Commerce authentication backend

Spring Boot 3.3.5 / Java 21 REST API for account registration, cookie-based sessions, password recovery, and password changes. Shopping features are outside this project.

## Local setup (Windows)

Install JDK 21+ and Maven 3.9+. From PowerShell:

```powershell
Set-Location C:\E-Commerce\E-Commerce-Backend
$env:SPRING_DATASOURCE_URL = 'jdbc:mysql://localhost:3306/ecommerce_auth?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true'
$env:SPRING_DATASOURCE_USERNAME = '<local database account>'
$env:SPRING_DATASOURCE_PASSWORD = '<local database password>'
$keyBytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($keyBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($keyBytes)
$env:SPRING_PROFILES_ACTIVE = 'local'
$env:CORS_ALLOWED_ORIGINS = 'http://localhost:5173'
$env:APP_COOKIE_SECURE = 'false'
$env:APP_COOKIE_SAME_SITE = 'Lax'
mvn spring-boot:run
```

The application runs on port 8080. Normal startup uses the configured datasource and Flyway; it can create or update schema objects. Confirm the datasource target before starting it. Do not point it at a database containing user data for tests. The test suite uses a separate in-memory H2 database and disables Flyway.

With the `local` profile active, recovery OTPs are printed in the backend console by the development-only delivery adapter; they are not returned to or displayed in the frontend. This is for local testing only.

For HTTPS deployments, set `APP_COOKIE_SECURE=true` and choose a reviewed `APP_COOKIE_SAME_SITE` policy appropriate to the frontend/API origin arrangement. Keep `CORS_ALLOWED_ORIGINS` limited to the exact trusted frontend origins. Do not use the `local` profile in production because it exposes recovery codes to the server console.

## Tests

```powershell
mvn test
```

Tests use `src/test/resources/application-test.yml` (`jdbc:h2:mem:auth_test`, schema created and dropped for the test run). They do not use MySQL. The current verified run passed 7 tests: 6 API/JPA integration tests and 1 rate-limiter unit test.

## API

All routes are under `/api`. Call `GET /api/csrf` first and send its `csrfToken` response value as `X-XSRF-TOKEN` on every state-changing request. The browser cookie is HttpOnly for authentication; the separate CSRF cookie is readable by the SPA. The API permits credentialed CORS only from configured origins.

| Method | Path | Access | Purpose |
|---|---|---|---|
| GET | `/api/csrf` | Public | Issue SPA CSRF token and cookie |
| POST | `/api/reg` | Public + CSRF | Register; phone is normalized to E.164 |
| POST | `/api/login` | Public + CSRF | Authenticate by email or phone; set HttpOnly session cookie |
| GET | `/api/me` | Session | Current account summary |
| GET | `/api/home` | Session | Protected welcome response |
| POST | `/api/logout` | Session + CSRF | Revoke current session |
| POST | `/api/forgot-password` | Public + CSRF | Start generic-response recovery flow |
| POST | `/api/verify-otp` | Public + CSRF | Verify recovery code and issue short-lived authorization |
| POST | `/api/reset-password` | Recovery authorization + CSRF | Reset password and revoke sessions |
| PUT | `/api/change-password` | Session + CSRF | Change password and revoke all sessions |

The Postman collection is `docs/api/E-Commerce-Auth.postman_collection.json`; it obtains the CSRF token before mutation calls.

## Handover documentation

- `docs/database/SCHEMA.md` and `docs/database/ER_DIAGRAM.md`
- `docs/handover/REQUIREMENTS_TRACEABILITY.md`
- `docs/handover/TROUBLESHOOTING.md`
- `docs/handover/VERIFIED_GAP_ASSESSMENT.md`
- `PROJECT_CONTEXT.md`

## Verified scope and limitations

Backend tests and the isolated local browser journey verified registration, duplicate rejection, email/phone login, logout, protected access, wrong current password, phone recovery through OTP verification, and CSRF/CORS behavior. Browser password-reset form submission awaits user handoff on the reset screen; reset success and session invalidation are covered by backend integration tests. MySQL persistence/migration was not verified in this work. Rate limiting is in-memory per process, and no email/SMS provider or production deployment review is included. These limits mean this project is not production-ready.
