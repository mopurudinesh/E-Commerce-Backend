# E-Commerce project context

Last updated: 2026-10-19 (initial workspace inventory)

## Purpose and source of truth

This workspace contains the user's existing e-commerce authentication project. The authoritative implementation requirements are in [`MASTER_IMPLEMENTATION_PLAN.md`](MASTER_IMPLEMENTATION_PLAN.md), including its add-on that defers automated testing and requires this context document to be maintained. Read this file before work, then verify every claim against the current code.

The workspace root is `C:\E-Commerce`. It contains two separate Git repositories:

- `E-Commerce-Frontend` — `https://github.com/mopurudinesh/E-Commerce-Frontend`
- `E-Commerce-Backend` — `https://github.com/mopurudinesh/E-Commerce-Backend`

The parent folder itself is not a Git repository. Keep frontend and backend code in their own repositories. The workspace-level `AGENTS.md`, this document, and the master plan are local shared guidance files in the parent folder. Each child repository also has its own `AGENTS.md` that points back to these shared files, so instructions load when either repository is opened directly.

## Current verified inventory

The current branch in both repositories is `main`, tracking `origin/main`. The only tracked file reported by `git ls-files` in each repository is its `README.md`.

Both repositories have substantial uncommitted local work. Preserve it:

- Frontend: `README.md` is modified; `.env.example`, `.gitignore`, `.oxlintrc.json`, `index.html`, `package-lock.json`, `package.json`, `public/`, `src/`, and `vite.config.js` are untracked.
- Backend: `.env.example`, `.gitignore`, `pom.xml`, and `src/` are untracked.

Do not reset, clean, overwrite, or discard these changes. The real `.env` files exist; do not expose or copy their values. No discovery of installed Java/Maven/Node/MySQL versions or service/database connectivity has been completed in this setup task.

### Frontend files observed

The frontend has a React/Vite JavaScript app with `src/App.jsx`, API modules, auth context, theme, protected route and reusable form components. Page files observed: Register, Login, Home, Forgot Password, Reset Password, and Not Found. The observed page inventory does not include OTP Verification or Change Password pages. This is only a filename inventory; implementation behavior has not been reviewed or verified.

The `package.json` scripts are `dev`, `build`, `lint`, and `preview`. Declared direct dependencies include React 19, React Router 7, MUI 9, Axios, Vite 8, and Oxlint. Manifest versions are declared ranges, not verified installed/runtime versions. No automated test scripts or test framework were observed in this manifest.

### Backend files observed

The backend is a Maven project with Spring Boot parent 3.3.5 and Java 21 configured in `pom.xml`. The source tree includes auth controller/service, DTOs, entities, repositories, JWT/security classes, phone validation, password recovery, an `application.yml`, and one Flyway migration `V1__create_auth_schema.sql`. Dependency declarations include Spring Web/Security/Data JPA/Validation, MySQL, Flyway, JJWT, libphonenumber, Springdoc, and Bucket4j. These are declared dependencies; compilation and runtime availability have not been verified.

The README contains only a heading. Application behavior, API contract conformance, schema correctness, migration effects, security controls, and startup state have not been verified.

## Planned scope and constraints

The master plan specifies a seven-screen React frontend and Java/Spring Boot/MySQL backend for registration, login, home, forgot password, OTP verification, reset password, and change password. It fixes required API routes and requires security controls, international phone normalization, migrations, documentation, and phased delivery. Consult the master plan for full details.

Automated tests are explicitly deferred by the add-on. Do not create or run automated tests unless the user explicitly changes that instruction. Basic development checks may be used when appropriate, and must be reported accurately.

The active milestone remains Phase 1 discovery and planning. No discovery report has been approved and no implementation was performed as part of creating these guidance files. Obtain approval before major implementation, destructive or administrative database work, repository pushes, deployment, or significant architecture changes, as specified in the plan.

## Phase 1 read-only discovery (2026-10-19)

- Environment: Java 25.0.4.1 and Node 24.15.0/npm 11.12.1 are available. The backend POM targets Java 21. The Maven command-line tool was not found. MySQL80 is running and `localhost:3306` accepted a TCP connection. No MySQL login was attempted, so the database's existence, schema, data, and connectivity using the app account remain unknown. No database changes were made.
- Frontend routes observed in `src/App.jsx`: `/`, `/home`, `/login`, `/register`, `/forgot-password`, `/reset-password`, and catch-all. There is no separate `/verify-otp` route or `/change-password` route in the route declarations. A change-password modal is present from HomePage. The API service has methods for OTP verification and password change. Homepage source also contains shopping metrics/catalog language, which is outside the approved milestone scope in the master plan.
- Backend `AuthController` maps registration, login, logout, forgot password, OTP verification, reset password, change password, `/me`, and `/home` endpoints. Security configuration applies authentication to the protected operations. This is source inspection only; the API has not been run.
- Backend source includes BCrypt, JWT/session entities, international phone validation, and one Flyway migration defining `users`, `user_sessions`, and `password_recovery_challenges`. Migration behavior and existing database compatibility have not been checked or applied.
- Authentication transport needs a decision/reconciliation before implementation: the backend source is configured for HttpOnly cookies, while frontend source also uses `withCredentials` and reads/writes bearer access and refresh tokens in `localStorage`. This is an observed code pattern, not a security validation.
- Frontend and backend source manifests include no verified automated test execution. A filename scan under their `src` trees found no test-named source files. Automated tests remain deferred by the add-on; none were run.
- Repository state remains as inventoried above, with the newly added `AGENTS.md` files untracked in each child repository. No application files were changed.
## Next step

Complete Phase 1 discovery: inspect both repository contents and Git state, verify local tool/service availability without assuming it, inspect database state without modifying it, compare existing code with the plan, and prepare the requested discovery report and phased implementation proposal. Wait for the user's approval before major implementation.

## Approved implementation progress (2026-10-19)

The user approved continuing beyond discovery. The preceding “Next step” and initial inventory above are historical Phase 1 notes; use this progress section and the actual repositories for current status.

- Frontend auth transport now consistently uses the backend's HttpOnly cookie. Raw access JWTs are no longer returned in the login response, loaded from localStorage, or sent from localStorage. The frontend no longer calls the nonexistent `/auth/refresh` endpoint. `user_info` remains only a display cache.
- The frontend now has a separate `/verify-otp` screen. Forgot-password routes to OTP verification; successful OTP verification sends the recovery token through in-memory router state to reset-password. Reset submits the expected recovery token, new password, and confirmation. Change-password UI now sends confirmation; the backend revokes sessions and clears the cookie, then the frontend clears local auth.
- Backend configuration no longer creates the database implicitly or falls back to MySQL root and a checked-in JWT key. It defaults to dedicated username `ecommerce_app`, requires password/JWT secret environment values, and exposes local cookie/CORS settings. The `.env.example` contains safe placeholders only; real `.env` files were not read or modified.
- OTP values are stored hashed. OTP resend cooldown, reuse rejection, invalid-attempt persistence, and a per-process in-memory rate limiter were added. A local-only console adapter now delivers codes when the explicit `local` Spring profile is active; no email/SMS provider exists. The in-memory limiter is not suitable as the only protection in a multi-instance production setup.
- A handover document is now in `E-Commerce-Backend/PROJECT_CONTEXT.md`; backend setup and limitations are also documented in its README.
- `npm run build` completed successfully in `E-Commerce-Frontend`. User screenshots confirm MySQL80 was running and TCP port 3306 was reachable, and Maven 3.10.0/JDK 25.0.4.1 are installed. `mvn -f pom.xml compile` then succeeded, compiling 33 Java files. No automated tests, Postman calls, backend startup, or migration were run. The agent execution shell cannot access the user's MySQL instance, so schema state remains unverified. No database was modified.
- Follow-up user-side SQL inspection confirmed schema `ecommerce_auth` exists and returned no tables. Maven compile succeeded after using the Maven installation shown by the user. The first backend start will apply Flyway V1 and create the three documented authentication tables; no manual table SQL is needed.
- Both repositories still contain the user's prior uncommitted work. Preserve it. See current Git status before any further edits.

### Current phase status

- Phase 1 — discovery: completed; implementation was authorized afterward.
- Phase 2 — setup: partially improved; safe config templates/docs updated, but Maven and MySQL environment readiness remain unresolved.
- Phases 3–6 — core backend/frontend implementation: existing code retained and integration gaps corrected; runtime behavior is not yet verified.
- Phase 7 — recovery: frontend flow and backend safeguards are implemented; local OTP delivery is available with the `local` profile. Runtime recovery remains unverified.
- Phase 8 — integration: frontend production build passed; backend connected to MySQL and Flyway applied V1. Authentication interactions still need a manual browser walkthrough.
- Phase 9 — handover: documentation updated; automated tests remain deferred as requested.

### Local run completed (2026-10-19)

- The user authorized applying the existing migration. MySQL 8.0 connected successfully; Flyway applied V1 to the empty `ecommerce_auth` schema. It now contains `users`, `user_sessions`, `password_recovery_challenges`, and Flyway's `flyway_schema_history`. No user rows were inserted.
- The backend is running on `http://localhost:8080` with the `local` profile. The active process uses a least-privilege `ecommerce_local` database account; its generated password remains only in the running process environment and is not stored in the repository.
- The frontend is running on `http://127.0.0.1:5173`; the login screen is open in the in-app browser. Frontend build and backend compile succeeded. Automated tests, Postman, and account registration/login/logout were not run.
- A MySQL administrator credential was supplied in chat. Do not repeat or store it; recommend rotating it after this setup.

Do not start the backend until the target database and application account are confirmed. Flyway may apply the existing `V1` migration during startup. The schema is described in `E-Commerce-Backend/PROJECT_CONTEXT.md`; no migration has been run by this agent.

## Frontend appearance update (2026-10-19)

- The user clarified the intended frontend style: light mode only, clean and professional, responsive, and Material Design-inspired, with a restrained blue accent. The earlier visual implementation used dark navy surfaces, purple gradients, glow effects, and glass-like cards; that did not match the user's requested appearance.
- Frontend inline component styles were updated to use white/light surfaces, dark readable text, subtle borders and shadows, and blue actions across the authentication screens and shared components. The theme helper now exposes only a light theme. The protected home page remains intentionally minimal, with its welcome message centered and enlarged; the navbar keeps Sign Out and has no brand name.
- `npm run build` succeeded after the light-theme changes. No automated tests were run. The authenticated home-page appearance and auth flows were not manually exercised in a browser during this update.

## Local URL troubleshooting (2026-10-19)

- The browser timeout was caused by stale duplicate Vite processes left from previous starts. They showed listeners on port 5173 but did not return HTTP responses. After stopping those Vite processes and starting one clean server with `--host 0.0.0.0 --port 5173 --strictPort`, a local HTTP request to `/login` returned status 200.
- At the time of this check, port 8080 had no listening backend process. Backend startup still requires `SPRING_DATASOURCE_PASSWORD` and `JWT_SECRET`; their values were not read from `.env`. The frontend page can load at `http://localhost:5173/login`, but authentication requests require the backend.

### Latest local runtime check (2026-10-19)

- After the Network Error screenshot, the backend was started with the `local` profile and a dedicated least-privilege MySQL account. No credential values were written to this document or the repositories. The generated database-account password exists only in the running backend process environment; a backend restart or computer restart will require secure credential provisioning before it can connect again.
- Verified listeners: Vite on port 5173, Spring Boot on port 8080, and MySQL on port 3306.
- Verified basic local responses: `GET http://127.0.0.1:5173/login` returned HTTP 200; `GET http://127.0.0.1:8080/v3/api-docs` returned HTTP 200; browser CORS preflight for `POST /api/login` from `http://localhost:5173` returned HTTP 200 and allowed that origin.
- These checks confirm the local pages/API are reachable and the browser is allowed to send the login request. They do not verify registration, login, logout, protected-page behavior, or recovery flows. No user credentials were submitted, no automated/Postman tests were run, and no database tables were changed during this troubleshooting.
- The earlier note above that port 8080 was not listening describes the state before this latest startup; use this subsection for the current checked state.

## Instructions for future agents

1. Read this file and `MASTER_IMPLEMENTATION_PLAN.md` before modifying the project.
2. Inspect both repositories, code, configuration, and Git status; do not rely solely on this summary.
3. Preserve existing work and follow the current architecture where it meets the approved requirements.
4. Avoid duplicating features already implemented; verify them in code.
5. Update this document whenever implementation changes, clearly separating implemented, planned, incomplete, and unverified behavior.
6. Implement only the features approved for the next phase.
7. Do not add automated tests unless the user explicitly requests them.


