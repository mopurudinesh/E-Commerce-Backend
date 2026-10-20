# E-Commerce authentication backend — project handover

Last reviewed: 2026-10-20. This guide explains the backend as implemented so another developer or AI agent can continue it without relying on chat history. Check the actual source and Git state before every change. The companion browser app is [E-Commerce-Frontend](https://github.com/mopurudinesh/E-Commerce-Frontend).

## Purpose and scope

This Spring REST API implements registration, email or international-phone login, cookie-based authentication, session tracking/revocation, profile and protected home endpoints, password recovery with OTP, reset, and authenticated password change. Shopping catalog, cart, checkout, payments, and order history are outside this milestone. Frontend appearance is light-only; the backend has no theme behavior.

## Current status and verification

- The fixed `/api` authentication and recovery routes are implemented (see API contract below).
- MySQL schema is defined in Flyway V1; Hibernate is configured to validate rather than create/update the production schema.
- Frontend/backend integration uses JWT credentials in an HttpOnly cookie, CSRF tokens for state-changing requests, and credentialed CORS restricted to the configured origin.
- Automated tests were explicitly authorized for the authentication completion work. The recorded test run passed 7 tests: 6 integration tests with isolated in-memory H2 and 1 rate-limiter test. In the test profile Flyway is disabled and Hibernate creates/drops the H2 schema. Tests did not target the user's regular MySQL database.
- The frontend browser walkthrough covered registration, duplicate rejection, email/phone login, logout, protected-route redirect, change-password wrong-current-password error, and phone recovery through OTP verification. The browser reset submission was not completed in that walkthrough; backend integration tests cover successful reset and session invalidation.
- MySQL persistence, real delivery provider integration, production HTTPS deployment, and deployment-specific cookie/CORS configuration are not verified. The project should not be represented as production-ready.

## Technology stack

Versions from Maven configuration: Java 21, Spring Boot 3.3.5, Maven, Spring Web, Spring Security, Spring Data JPA/Hibernate, Jakarta Bean Validation, MySQL Connector/J, Flyway, BCrypt, JJWT 0.12.6, Google libphonenumber 8.13.47, Springdoc OpenAPI 2.6.0, JUnit 5 and Spring Boot Test. Springdoc is declared at 2.6.0. Bucket4j is also declared as a dependency, but current rate checks are implemented by the project-local `InMemoryRateLimiter` (not Bucket4j).

## Architecture and request/data flow

```text
React browser (localhost:5173)
  ├─ gets CSRF token; sends credentials + X-XSRF-TOKEN
  └─ calls http://localhost:8080/api
       ↓
AuthController → DTO validation → AuthService / PasswordRecoveryService
       ↓                                  ↓
Security filter chain/JWT session check   repositories (Spring Data JPA)
       ↓                                  ↓
HttpOnly access cookie + CSRF cookie     MySQL ecommerce_auth (Flyway schema)
```

Controllers accept/return DTOs, not JPA entities. Services apply account, password, phone, recovery, and session rules. Repositories persist `User`, `UserSession`, and `PasswordRecoveryChallenge`. A JWT contains a session identifier (`jti`); the backend checks that identifier against a live server-side session record on authenticated requests. This lets logout and password events revoke sessions before JWT expiry.

## Repository map

```text
src/main/java/com/ecommerce/auth/
├── AuthBackendApplication.java         Spring Boot entry point
├── controller/AuthController.java      HTTP routes, status codes, CSRF endpoint, rate checks
├── dto/                                Validated request and safe response types
├── entity/                             User, UserSession, PasswordRecoveryChallenge
├── exception/                          Application exceptions and safe error mapping
├── repository/                         Spring Data JPA persistence interfaces
├── security/                           SecurityConfig, JWT provider/filter, principal and entry point
└── service/
    ├── AuthService.java                Registration, login, logout, profile, password change
    ├── PasswordRecoveryService.java    OTP lifecycle, recovery authorization, reset
    ├── PhoneValidationService.java     libphonenumber validation and E.164 normalization
    ├── InMemoryRateLimiter.java        Per-process IP rate limits
    ├── RecoveryCodeDelivery.java       Delivery interface
    ├── LocalConsoleRecoveryCodeDelivery.java  Local profile OTP output
    └── DisabledRecoveryCodeDelivery.java       Non-local no-provider delivery
src/main/resources/
├── application.yml                    datasource, Flyway/JPA, JWT, cookies, CORS, OpenAPI
└── db/migration/V1__create_auth_schema.sql  Initial MySQL schema
src/test/                               Integration and unit tests
src/test/resources/                     Isolated H2 test configuration
```

`pom.xml` is the authoritative dependency/version source. `.env.example` contains names/placeholders only; do not read, copy, or commit real `.env` values.

## API contract

Base URL: `http://localhost:8080`; controller base path: `/api`. Swagger UI is available locally at `/swagger-ui.html`; OpenAPI JSON is `/v3/api-docs` when the service is running.

| Method and path | Authentication | Purpose / request fields |
|---|---|---|
| `GET /api/csrf` | Public bootstrap | Returns `{ "csrfToken": "..." }` and sets CSRF cookie. |
| `POST /api/reg` | Public + CSRF | JSON: `fullName`, `email`, `mobileNumber`, optional `phoneRegion`, `password`, `confirmPassword`. Returns 201 and safe user summary. |
| `POST /api/login` | Public + CSRF | JSON: `identifier`, `password`; identifier can be email or international phone. Sets access cookie. Generic credential failure. |
| `POST /api/logout` | Authenticated + CSRF | Revokes current server session, clears auth cookie(s), returns 204. |
| `POST /api/forgot-password` | Public + CSRF | JSON: `identifier`; generic response to avoid account disclosure. |
| `POST /api/verify-otp` | Public + CSRF | JSON: `identifier`, six-digit `otp`; returns a short-lived `recoveryToken` after successful verification. |
| `POST /api/reset-password` | Recovery token + CSRF | JSON: `recoveryToken`, `newPassword`, `confirmPassword`; consumes token and revokes all user's sessions. |
| `PUT /api/change-password` | Authenticated + CSRF | JSON: `currentPassword`, `newPassword`, `confirmPassword`; validates current password and revokes sessions per service policy. |
| `GET /api/me` | Authenticated | Safe current-user profile only. |
| `GET /api/home` | Authenticated | JSON `{ "message": "Welcome to Home Page" }`. |

Registration response is an `ApiResponse` envelope with safe user data. Other API responses use that envelope where applicable. Errors are mapped by `GlobalExceptionHandler` to structured safe responses. Confirm DTOs and controller when evolving the contract; keep the frontend `authService.js` aligned. Passwords require at least 8 chars including uppercase, lowercase, digit, and an allowed special character; confirmation equality is checked by service logic.

## Database schema

Flyway migration: `src/main/resources/db/migration/V1__create_auth_schema.sql`. Database default is `ecommerce_auth` on local MySQL port 3306. The migration creates:

- `users`: identity (`id`), `full_name`, unique normalized `email`, unique canonical `mobile_e164`, `phone_region`, BCrypt `password_hash`, timestamps, and `password_changed_at`.
- `user_sessions`: FK `user_id` to users, unique JWT `jti`, creation/expiry and nullable revocation timestamp. User deletion cascades.
- `password_recovery_challenges`: FK user, hashed OTP, created/expiry, attempt count, consumed timestamp, delivery channel, hashed reset authorization and its expiry. User deletion cascades.
- Flyway's own `flyway_schema_history` table is maintained by Flyway.

Never store a plaintext password, OTP, JWT, or recovery token. Do not run the production/default migration against a database with existing data without first confirming the datasource target and reviewing schema/data implications. Tests use in-memory H2 at `jdbc:h2:mem:auth_test` and do not connect to MySQL.

The ER relationship is one user to many sessions and one user to many recovery challenges; the two child tables reference `users.id`. Detailed schema and ER diagram: [`docs/database/SCHEMA.md`](docs/database/SCHEMA.md).

## Authentication, CSRF, and security behavior

- Passwords are hashed using BCrypt (configured work factor 12).
- JWT access token is placed in `ecommerce_access_token` HttpOnly cookie. Server session metadata stores `jti`, user id, expiry, and revocation; the raw JWT is not persisted in the session table.
- The access token default lifetime is 15 minutes. The YAML also declares a refresh-cookie name and 7-day refresh expiration; verify actual issuance and frontend use in source before extending refresh behavior. The current frontend uses the access cookie and session hydration.
- The JWT filter supports cookie authentication and also recognizes an Authorization Bearer token. The browser integration uses the cookie.
- Spring Security uses `CookieCsrfTokenRepository`: `XSRF-TOKEN` cookie and `X-XSRF-TOKEN` request header. The SPA first fetches `GET /api/csrf`, then includes the token on state-changing requests.
- Credentialed CORS allows the configured explicit origin, default `http://localhost:5173`; do not use wildcard origins with credentials.
- Local HTTP defaults: `APP_COOKIE_SECURE=false`, `APP_COOKIE_SAME_SITE=Lax`. HTTPS deployments require `APP_COOKIE_SECURE=true`; choose SameSite and any cross-site cookie behavior to match the actual frontend/backend domain topology, and verify CSRF/CORS in that deployment. Do not deploy with localhost settings.
- BCrypt password validation, generic login/recovery responses, input validation, and safe exception mapping are applied server-side.

## Password recovery and limits

OTP defaults are 6 digits, 5-minute validity, 5 attempts per challenge, and 60-second resend cooldown. OTPs and reset authorization values are hashed in storage. Successful OTP verification consumes that OTP and returns a short-lived, single-use reset authorization (10-minute expiry). Successful reset changes password and revokes all active sessions.

Current per-IP rate checks are held in `InMemoryRateLimiter`: login 10 attempts per 15 minutes, forgot-password 5 per hour, OTP verification 10 per 15 minutes. The limiter is process-local and resets on restart; it is not a distributed production limiter and should be replaced or backed by shared storage before multi-instance deployment.

When `SPRING_PROFILES_ACTIVE=local`, `LocalConsoleRecoveryCodeDelivery` prints the generated development OTP to the **backend terminal/console**. The API does not return it and the frontend does not display it. Outside local profile, `DisabledRecoveryCodeDelivery` is used unless a real delivery adapter is configured; successful external SMS/email delivery is not implemented. Do not enable local OTP logging in production.

## Configuration and local Windows setup

Configuration is read from environment variables; the project does not automatically load a `.env` file. Common settings:

| Variable | Purpose | Local example or safe form |
|---|---|---|
| `SPRING_DATASOURCE_URL` | JDBC target | `jdbc:mysql://localhost:3306/ecommerce_auth?...` |
| `SPRING_DATASOURCE_USERNAME` | Dedicated app DB account | `<local application account>` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | `<local password>` |
| `JWT_SECRET` | HMAC signing key, at least 256 bits | Generate a random local key; never commit it. |
| `JWT_ACCESS_EXPIRATION_MS` | Access-token lifetime | Defaults to 900000 ms. |
| `JWT_REFRESH_EXPIRATION_MS` | Refresh lifetime setting | Defaults to 604800000 ms. |
| `SPRING_PROFILES_ACTIVE` | Local recovery adapter | Use `local` only for local OTP console delivery. |
| `CORS_ALLOWED_ORIGINS` | Explicit browser origin | `http://localhost:5173` |
| `APP_COOKIE_SECURE` | Cookie Secure flag | `false` for local HTTP; `true` for HTTPS. |
| `APP_COOKIE_SAME_SITE` | Cookie SameSite setting | `Lax` for local same-site development. |

Prerequisites: JDK 21+, Maven 3.9+, and a running local MySQL Server. Before start, confirm the datasource points only at the intended local test/development database. From PowerShell:

```powershell
Set-Location C:\E-Commerce\E-Commerce-Backend
$env:SPRING_DATASOURCE_URL = 'jdbc:mysql://localhost:3306/ecommerce_auth?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true'
$env:SPRING_DATASOURCE_USERNAME = '<local application account>'
$env:SPRING_DATASOURCE_PASSWORD = '<local password>'
$keyBytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($keyBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($keyBytes)
$env:SPRING_PROFILES_ACTIVE = 'local'
$env:CORS_ALLOWED_ORIGINS = 'http://localhost:5173'
$env:APP_COOKIE_SECURE = 'false'
$env:APP_COOKIE_SAME_SITE = 'Lax'
mvn spring-boot:run
```

Keep the terminal open. The command uses Flyway against the configured datasource on application startup; verify the target before running. The default local database account in `application.yml` is `ecommerce_app`, but use only an account that has already been safely provisioned; this guide does not create databases/users or grant privileges.

Useful commands:

```powershell
mvn test
mvn spring-boot:run
```

Frontend startup and full local workflow are in the frontend repository README and context guide.

## Tests and other handover artifacts

- Test configuration is under `src/test/resources`; it uses isolated in-memory H2, `ddl-auto=create-drop`, and disables Flyway.
- Recorded run: `mvn test` passed 7 tests (6 integration, 1 rate-limiter). Test coverage includes core registration/auth/recovery/session and CSRF behavior; inspect current tests for precise coverage before relying on any individual scenario.
- OpenAPI/Swagger UI: `/swagger-ui.html` while running.
- Postman collection: [`docs/api/E-Commerce-Auth.postman_collection.json`](docs/api/E-Commerce-Auth.postman_collection.json).
- Database schema and ER diagram: [`docs/database/SCHEMA.md`](docs/database/SCHEMA.md).
- Requirements mapping: [`docs/handover/REQUIREMENTS_TRACEABILITY.md`](docs/handover/REQUIREMENTS_TRACEABILITY.md).
- Troubleshooting: [`docs/handover/TROUBLESHOOTING.md`](docs/handover/TROUBLESHOOTING.md).

## Instructions for future contributors and agents

1. Read this guide and `AGENTS.md`, then inspect the actual code, Git status, current branch, remotes, configuration, and tests in both repositories.
2. Preserve existing changes and maintain the current layered architecture. Do not duplicate existing routes, DTOs, services, security filters, or migrations.
3. Confirm this guide against source; it is a map, not an authority over code. Update it whenever implementation or verification changes.
4. Keep backend code in this repository and frontend code in the companion repository. Coordinate API, cookie, CSRF, CORS, and phone contract changes across both.
5. Keep secrets out of Git and reports; do not inspect `.env` values. Use safe placeholders.
6. Never run Flyway against an unknown or user-data database. Use isolated test configuration for automated tests; ask before destructive/admin DB actions.
7. Automated tests were explicitly authorized for the authentication completion task. Follow the latest user instruction for future test work.
8. Implement only the next requested feature. Do not add shopping functionality unless asked.
