# E-Commerce-Backend

## Local development

This is the Spring Boot authentication API. It uses MySQL and Flyway migrations. The application account is configured as `ecommerce_app`; do not use MySQL root for normal app access. The database and account must exist before startup. The application URL no longer asks MySQL to create the database automatically.

The backend requires Java 21 or newer and Maven. Maven 3.10.0 with JDK 25.0.4.1 was verified. The backend has started locally on port 8080; Flyway applied V1 successfully to `ecommerce_auth`.

In PowerShell, open a new terminal and set local environment values (use your own database password; do not commit it):

```powershell
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

Flyway applies the checked-in migration during startup. V1 has been applied to the confirmed empty `ecommerce_auth` schema. For local HTTPS deployments, set `APP_COOKIE_SECURE=true` and review the `SameSite` policy.

## API overview

The fixed authentication routes are `POST /api/reg`, `POST /api/login`, `POST /api/logout`, `POST /api/forgot-password`, `POST /api/verify-otp`, `POST /api/reset-password`, `PUT /api/change-password`, `GET /api/me`, and `GET /api/home`. The login cookie is HttpOnly; raw access JWTs are not returned in the login response. The browser frontend is configured for `http://localhost:5173`.

## Current limitations

- For local recovery, start the backend with `SPRING_PROFILES_ACTIVE=local`; the 6-digit OTP is printed to the backend terminal with a local-only warning. The local profile must not be used in production. Without that profile, delivery is disabled until an email/SMS provider is configured.
- Rate limits are in-memory and apply to one backend process; production deployments need a shared rate-limit store or gateway policy.
- Frontend build and backend compile succeeded. Database migration and backend startup succeeded; registration/login/logout and protected-home behavior still need a manual browser walkthrough. Automated and Postman tests are deferred.
- See [`PROJECT_CONTEXT.md`](PROJECT_CONTEXT.md) for the implementation inventory, schema, API details, and handover status.
