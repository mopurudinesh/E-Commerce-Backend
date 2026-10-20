# E-Commerce authentication backend context

Last reviewed: 2026-10-20. Read the workspace-level `C:\E-Commerce\PROJECT_CONTEXT.md` for the joint project status and verified manual journey.

## Actual backend state

Spring Boot 3.3.5 / Java 21 authentication API. It supports registration, email-or-phone login, HttpOnly session cookies, protected `/api/me` and `/api/home`, logout, OTP recovery, password reset, and password change. Phone values are validated and normalized to E.164 with libphonenumber. CSRF is enabled for state-changing requests: `GET /api/csrf` issues the SPA token, which must be sent as `X-XSRF-TOKEN`. Credentialed CORS is restricted to configured origins.

Tests are isolated with H2 in `src/test/resources/application-test.yml`; Flyway is disabled in that profile. Verified `mvn test -q`: 7 passed (6 API/JPA integration tests and 1 rate limiter unit test). MySQL persistence and migrations were not verified in this work. The current development browser process uses an in-memory H2 database. No `.env` values were read.

## Safe runtime notes

Normal startup uses `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`, then Flyway applies migrations to that configured database. Verify the target first. Do not run automated tests against the normal datasource. Local HTTP defaults to cookie Secure=false and SameSite=Lax; configure Secure=true and review SameSite for HTTPS deployments. The `local` profile logs development OTPs and must not be used in production. Rate limiting is per-process/in-memory; email/SMS delivery and production deployment review remain outstanding.

See `README.md`, `docs/api/`, `docs/database/`, and `docs/handover/` for setup and handover details. Keep this context accurate as code changes.
