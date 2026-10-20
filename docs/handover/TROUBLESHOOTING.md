# Troubleshooting

## Browser reports a CSRF 403

- Open the app and API on localhost, not a mixture of localhost and 127.0.0.1.
- Confirm the frontend origin exactly matches CORS_ALLOWED_ORIGINS (local default http://localhost:5173). Credentials cannot be used with wildcard origins.
- Axios first calls GET /api/csrf, stores the readable XSRF-TOKEN cookie, and sends its response token as X-XSRF-TOKEN on mutations. Clear site cookies after changing the backend origin.
- Check the browser request includes credentials and the header. Never make the access-token cookie readable to JavaScript.

## Login or protected pages return 401

- Confirm the backend listens on port 8080 and frontend VITE_API_BASE_URL ends in /api.
- Check the login response sets the HttpOnly ecommerce_access_token cookie and subsequent requests include it.
- Use the same hostname (localhost) for frontend and API. SameSite=Lax blocks cookies on cross-site fetches.
- A logged-out, expired, or revoked server session must be rejected. Sign in again after password reset or password change; both operations revoke sessions.

## Database connection or migration error

- Inspect SPRING_DATASOURCE_URL, username, and password variable names without pasting secret values into logs or chat.
- Startup with the normal profile runs Flyway. Confirm the schema target and take a safe backup before any normal database migration. Tests use only in-memory H2.
- Do not set ddl-auto to update or create the database implicitly. Hibernate validates the Flyway schema.

## Recovery code cannot be used

- The local console delivery adapter exists only when the local Spring profile is active; look in the backend terminal for the generated six-digit code.
- Codes expire after 5 minutes, have a 60-second resend cooldown, and permit at most 5 attempts. Request a fresh code after expiry/lockout.
- OTP delivery outside local mode is unconfigured. A real email/SMS provider is not included.

## Node or Maven commands fail

- Frontend: use supported Node.js, then run npm install in E-Commerce-Frontend.
- Backend: install JDK 21+ and Maven 3.9+; run Maven from E-Commerce-Backend.
- A Maven artifact-cache lock error can be transient if another Maven process is active; wait for it to finish before retrying.
