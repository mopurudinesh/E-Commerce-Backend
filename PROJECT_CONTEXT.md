# E-Commerce Authentication Project Context

Last reviewed: 2026-10-19

## Purpose and scope

This project is an authentication module for a React/Vite frontend and Spring Boot backend. The current demonstration is registration, login, logout, and a protected home page showing **“Welcome to Home Page!”**. The agreed related backend scope also includes forgot-password, OTP verification, reset-password, change-password, and JWT-backed session management. Product catalogs, shopping carts, and payments are outside this scope.

This file describes the actual local code at the review date. It does not claim that the backend or database has been run successfully.

## Repository state and preservation

The workspace contains two repositories on `main`, each tracking `origin/main`. Both contain local changes that must be preserved. At the time of review, the frontend had a modified README and untracked project files; the backend had untracked application/configuration files. Do not run `git clean`, reset, checkout over files, or push these changes without explicit approval. No `.env` values were read or copied.

## Technology and tools

- Frontend: React 19, Vite 8, JavaScript, React Router 7, Axios, MUI, and custom CSS.
- Backend: Java, Spring Boot 3.3.5, Spring Security, Spring Data JPA, Hibernate, Maven, Flyway, MySQL JDBC, JJWT, Bean Validation, libphonenumber, Springdoc, and a small in-memory rate limiter.
- Database: MySQL is configured in `application.yml`; the project contains one Flyway migration.
- Node 24.15.0 and npm 11.12.1 were available. The frontend production build succeeded with `npm run build`.
- The user verified MySQL80 Running and `127.0.0.1:3306` reachable. Flyway V1 has now run successfully against `ecommerce_auth` and created the authentication tables.
- The user verified Maven 3.10.0 with JDK 25.0.4.1. `mvn -f pom.xml compile` succeeded from the backend repository and compiled 33 Java source files with Java release 21. Maven emitted metadata warnings for optional/unavailable repositories; compilation succeeded. The app was not started.
- The existing `ecommerce_app@localhost` account has database-scoped grants; its password was not read or changed. The active backend run uses a separate least-privilege `ecommerce_local@localhost` account. Its generated password is held only in the active backend process environment.

## Folder map

### Frontend: `E-Commerce-Frontend`

- `src/App.jsx` — browser routes and `AuthProvider` setup.
- `src/api/axios.js` — Axios instance, API base URL, and credentialed cookie requests.
- `src/api/authService.js` — wrappers for the fixed backend endpoints.
- `src/context/AuthContext.jsx` — login state, `/api/me` initialization, and logout.
- `src/components/ProtectedRoute.jsx` — waits for auth initialization and sends signed-out users to login.
- `src/components/Navbar.jsx` — account label and sign-out action.
- `src/pages/RegisterPage.jsx`, `LoginPage.jsx`, `HomePage.jsx` — core demonstration screens.
- `src/pages/ForgotPasswordPage.jsx`, `VerifyOtpPage.jsx`, `ResetPasswordPage.jsx` — recovery screens.
- `src/components/PhoneNumberInput.jsx` — current country/phone input.
- `src/theme/theme.js`, `src/index.css`, `src/App.css` — existing theme and styling.
- `.env.example` — safe frontend API URL placeholder.

The existing home page retains profile/security and shopping-themed display sections. The shopping widgets are not backed by commerce APIs and are outside the requested demonstration; they have not been removed. The phone selector currently has a limited fixed country list.

### Backend: `E-Commerce-Backend`

- `src/main/java/com/ecommerce/auth/controller/AuthController.java` — fixed REST routes.
- `service/AuthService.java` — registration, login, logout, current user, and password changes.
- `service/PasswordRecoveryService.java` — OTP challenge and reset-token logic.
- `service/PhoneValidationService.java` — phone validation and E.164 normalization.
- `service/InMemoryRateLimiter.java` — per-process request windows for login and recovery routes.
- `security/JwtTokenProvider.java`, `JwtAuthenticationFilter.java`, `SecurityConfig.java` — JWT parsing, session validation, access rules, and CORS.
- `entity/User.java`, `UserSession.java`, `PasswordRecoveryChallenge.java` — JPA models.
- `repository/*Repository.java` — persistence queries.
- `dto/*` — request and response models.
- `src/main/resources/application.yml` — runtime configuration via environment variables.
- `src/main/resources/db/migration/V1__create_auth_schema.sql` — initial schema migration.
- `pom.xml` — Maven dependencies and Java target configuration.
- `.env.example` — safe configuration names and placeholders; it is not loaded automatically by Spring Boot.

## Request and authentication flow

1. Registration posts the full name, email, phone, phone region, password, and confirmation to `POST /api/reg`. The backend validates the request, normalizes the phone number, checks duplicates, BCrypt-hashes the password, and saves the user.
2. Login posts an email or phone identifier and password to `POST /api/login`. On success, the backend creates a session row and signed access JWT, then sets the JWT in an HttpOnly cookie. The response includes user summary and expiry information; it does not return the raw JWT.
3. The browser sends the cookie on API calls. `JwtAuthenticationFilter` validates the JWT and checks that its session row still exists and is not revoked or expired. `/api/me` hydrates frontend auth state after load.
4. `/api/home` requires authentication and returns a welcome message. The page formats it as **“Welcome to Home Page!”**.
5. Logout posts to `/api/logout`; the backend revokes the current session and clears the cookie. A successful password change revokes sessions and clears the current cookie as well. The frontend clears its cached user summary. The `user_info` localStorage value is display data only, not an authentication token.

The frontend and backend have been aligned to cookie authentication. The frontend no longer sends bearer tokens from localStorage or calls the backend's nonexistent refresh endpoint. There is no refresh-token API implementation. The backend uses `SameSite=Lax`; Spring CSRF protection is currently disabled. This is suitable only as a local demonstration posture and needs a reviewed CSRF/deployment policy before production use.

## API routes

| Method and path | Access | Current behavior |
|---|---|---|
| `POST /api/reg` | Public | Creates an account; returns 201 and a safe user summary. |
| `POST /api/login` | Public | Verifies credentials, creates a server-side session, and sets an HttpOnly access cookie. |
| `POST /api/logout` | Authenticated | Revokes current session and clears the cookie; returns 204. |
| `POST /api/forgot-password` | Public | Generic response and recovery challenge creation when account exists. Local profile prints the code to the backend terminal. |
| `POST /api/verify-otp` | Public | Checks OTP expiry/attempts and returns short-lived recovery authorization. |
| `POST /api/reset-password` | Recovery authorization | Sets a new password and revokes active sessions. |
| `PUT /api/change-password` | Authenticated | Verifies current password, updates it, and revokes active sessions. |
| `GET /api/me` | Authenticated | Returns safe profile fields. |
| `GET /api/home` | Authenticated | Returns `{"message":"Welcome to Home Page"}`. |

## Database schema

The migration defines these three tables. Their actual presence in MySQL has **not** been confirmed, and no migration or database modification was performed during this review.

| Table | Fields and purpose |
|---|---|
| `users` | `id` primary key; `full_name`; unique `email`; unique `mobile_e164`; optional `phone_region`; `password_hash`; `created_at`; `updated_at`; optional `password_changed_at`. Stores account identity and a BCrypt hash, never a plaintext password. |
| `user_sessions` | `id`; `user_id` foreign key to `users.id` with cascade delete; unique `jti`; `created_at`; `expires_at`; optional `revoked_at`. Makes a JWT session immediately revocable. |
| `password_recovery_challenges` | `id`; `user_id` foreign key to `users.id` with cascade delete; `otp_hash`; `created_at`; `expires_at`; `attempt_count`; optional `consumed_at`; `delivery_channel`; optional `recovery_token_hash`; optional `recovery_expires_at`. Stores hashed recovery material and expiry/attempt state. |

No additional application table is proposed. `users` and `user_sessions` support the core flow; `password_recovery_challenges` supports the agreed recovery features. Flyway also creates its `flyway_schema_history` bookkeeping table. `ddl-auto` is `validate`; Flyway owns schema changes. The JDBC URL no longer uses `createDatabaseIfNotExist` and uses the dedicated app username by default.

## Security and configuration

- Passwords are encoded with BCrypt (work factor 12).
- JWT signing key is required through `JWT_SECRET`; there is no application.yml fallback key.
- Database URL, username, and password use `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`. The default username is `ecommerce_app`; the database/account must be provisioned separately.
- Cookie options use `APP_COOKIE_SECURE` and `APP_COOKIE_SAME_SITE`. Local HTTP defaults are `false` and `Lax`; HTTPS deployments must set Secure and review the policy.
- CORS defaults to `http://localhost:5173` and allows credentials.
- Login and recovery limits are in-memory and per backend process. They are not sufficient for a multi-instance production deployment.
- OTPs are hashed in storage. The `local` Spring profile provides a development-only console delivery adapter so a learner can complete recovery without an email/SMS provider; outside that profile, delivery stays disabled. Never enable the `local` profile in production. Recovery still requires runtime verification.
- `.env.example` files contain placeholders only. Spring Boot does not load `.env` automatically; set environment variables in the terminal that launches the backend. Never copy real credentials into documentation or Git.

## Running locally

### Frontend

From PowerShell:

```powershell
Set-Location C:\E-Commerce\E-Commerce-Frontend
npm run dev
```

The frontend uses `VITE_API_BASE_URL=http://localhost:8080/api` from its environment or default. `npm run build` was run successfully after the frontend changes. Automated tests and Postman checks were not run.

### Backend

From PowerShell, set database credentials and generate a local Base64 key before running:

```powershell
Set-Location C:\E-Commerce\E-Commerce-Backend
$env:SPRING_DATASOURCE_URL = 'jdbc:mysql://localhost:3306/ecommerce_auth?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true'
$env:SPRING_DATASOURCE_USERNAME = 'ecommerce_app'
$env:SPRING_DATASOURCE_PASSWORD = '<your local ecommerce_app password>'
$env:SPRING_PROFILES_ACTIVE = 'local'
$secretBytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($secretBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($secretBytes)
$env:CORS_ALLOWED_ORIGINS = 'http://localhost:5173'
$env:APP_COOKIE_SECURE = 'false'
$env:APP_COOKIE_SAME_SITE = 'Lax'
mvn spring-boot:run
```

The backend has been compiled and started with the `local` profile. MySQL 8.0 accepted the connection and Flyway applied V1 successfully. The current process uses the separate local account noted above. Automated tests and Postman checks were not run.

## Current status and remaining work

- Core registration/login/logout/home code exists and the frontend now follows the backend cookie session flow. Frontend build and backend startup succeeded; authentication interactions remain unverified because no user account has been created.
- OTP verification and reset frontend screens now call the backend in the correct sequence and keep recovery authorization in navigation state (not persistent storage). Local-only console delivery is implemented behind the explicit `local` Spring profile; an email/SMS provider remains out of scope for local demo.
- Password change UI now sends confirmation; the backend revokes sessions and clears the current cookie, then the frontend clears its auth state.
- Backend has an in-memory login/recovery rate limiter, OTP resend cooldown, consumed-code check, and committed attempt counter after invalid OTP attempts. Backend compilation succeeded after the local delivery adapter was added.
- Refresh-token support is absent and should remain deferred unless specifically needed; the current access-cookie flow has no refresh route.
- Database migration and backend startup are verified. End-to-end auth behavior remains to be walked through manually. Automated tests and Postman are explicitly deferred.
- Existing home page retains commerce-themed widgets outside current scope. They are not backed by commerce APIs and should not be expanded.

## Guidance for the next agent

1. Read this file plus the workspace `AGENTS.md`, `PROJECT_CONTEXT.md`, and `MASTER_IMPLEMENTATION_PLAN.md` before editing.
2. Inspect both repositories and Git status; verify these notes against actual source.
3. Preserve uncommitted work and do not reset, clean, overwrite, or push it.
4. Do not create/drop/alter tables or run Flyway until the target DB/schema is known and approved. Do not read `.env` values.
5. Keep work within authentication scope. Do not add products, cart, or payment features.
6. Do not add or run automated tests or Postman tests unless the user changes that instruction.
7. Update this document with actual changes and clearly label behavior that has not been verified.
