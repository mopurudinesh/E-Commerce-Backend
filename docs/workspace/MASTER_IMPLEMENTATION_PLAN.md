# MASTER AGENT PROMPT
## Complete E-Commerce Authentication System — Frontend, Backend, Database and Security

### 1. Your role

Act as a senior full-stack software engineer, software architect, database designer, security engineer, and QA engineer.

Build a complete, professional authentication system for my e-commerce project using my existing GitHub repositories and local Windows development environment.

I am learning Java and will learn JavaScript in my classes later. Therefore, write beginner-friendly, readable code and explain important concepts, implementation decisions, and commands when handing over each phase.

Prioritize correctness, security, maintainability, and clear documentation over generating code quickly.

**Important: Begin with discovery and planning. Do not immediately generate the entire application.**

---

### 2. Existing GitHub repositories

I have already created two GitHub repositories.

**Frontend repository:**  
[https://github.com/mopurudinesh/E-Commerce-Frontend]

**Backend repository:**  
[https://github.com/mopurudinesh/E-Commerce-Backend]

#### Mandatory repository rules

1. Use these existing repositories. Do not create replacement repositories.
2. Keep frontend and backend code in their respective repositories.
3. Inspect existing files, branches, Git remotes, Git status, README files, and configuration before making changes.
4. Preserve existing code and uncommitted work.
5. Do not overwrite or delete existing files without my approval.
6. Do not push commits, change repository settings, deploy the application, or perform other remote GitHub operations without my explicit authorization.
7. If a repository is empty, initialize the appropriate project in it.
8. If it already contains code, understand the existing structure before deciding what to change.
9. If access to a private repository is unavailable, explain what authorization is needed.
10. Never put credentials, passwords, signing keys, or other secrets in GitHub.

---

### 3. Project objective and complete scope

Build a complete authentication system for an e-commerce website.

The application must support account registration, login, protected home access, logout, forgot-password, OTP verification, password reset, and authenticated password change.

 **this version must include all seven frontend pages or distinct route-level screens listed below.**

#### Required frontend pages

1. Registration
2. Login
3. Welcome Home
4. Forgot Password
5. OTP Verification
6. Reset Password
7. Change Password

Do not add product listings, product details, shopping cart, checkout, payments, order history, or other shopping functionality in this milestone.

#### Required backend functionality

- Registration and validation.
- Login using email or international phone number.
- JWT-based authentication with server-side session tracking and revocation.
- Protected user profile and home endpoints.
- Logout and session revocation.
- Forgot-password recovery initiation.
- OTP verification.
- Password reset.
- Authenticated password change.
- International phone number validation.
- Secure MySQL persistence.
- Rate limiting and recovery security.
- API documentation.
- Automated tests.

---

### 4. Technology stack

Use the following stack unless an existing repository constraint requires an approved change.

#### Frontend

- JavaScript, not TypeScript.
- React.
- Vite.
- Material UI (MUI).
- React Router.
- CSS and MUI themes.
- A maintained international phone input component and validation library.
- Vitest.
- React Testing Library.

Use `.jsx` for React components and `.js` for regular JavaScript modules.

Initialize Vite with its JavaScript template.

Do not introduce TypeScript, `.tsx` files, interfaces, type annotations, or TypeScript configuration.

Do not add Tailwind CSS unless I approve the change.

#### Backend

- Java.
- Spring Boot.
- Maven.
- Spring Web.
- Spring Security.
- Spring Data JPA and Hibernate.
- MySQL.
- Jakarta Bean Validation.
- BCrypt password hashing.
- A maintained JWT library.
- Google libphonenumber for Java, or an equivalent maintained library.
- Flyway.
- Springdoc OpenAPI/Swagger UI.
- JUnit.
- Spring Boot Test.
- MockMvc where appropriate.

Use a clear layered architecture consisting of controllers, DTOs, services, repositories, entities, security configuration, exception handling, recovery functionality, and application configuration.

Use readable Java and conventional object-oriented programming practices.

---

### 5. Phase 1: discovery and planning only

Before major implementation, inspect both repositories and the available local environment.

Verify:

- Existing repository contents, branches, remotes, and uncommitted changes.
- Java, Maven, Node.js, and npm availability.
- MySQL Server availability and connectivity.
- Whether the proposed database already exists.
- Whether existing tables or data could be affected.
- Existing frontend and backend configurations.
- Required environment variables and dependencies.
- Any existing implementation that should be preserved.

Present a discovery report containing:

1. Repository assessment.
2. Environment assessment.
3. Proposed folder structures.
4. Database schema and migration plan.
5. API contract.
6. International phone number strategy.
7. Authentication and session design.
8. Password recovery and OTP security design.
9. Frontend page flows.
10. Testing plan.
11. Risks, assumptions, and unresolved decisions.
12. Proposed implementation phases.

**Wait for my approval before major implementation or administrative database changes.**

Do not claim to have inspected resources that you could not actually access.

---

### 6. Local development environment

This project runs locally on Windows.

| Component | Address |
|---|---|
| Frontend | `http://localhost:5173` |
| Backend | `http://localhost:8080` |
| MySQL | `localhost:3306` |
| Frontend API base URL | `http://localhost:8080/api` |

MySQL Server, MySQL Workbench, and the related SQL tools are already installed.

Do not reinstall them unnecessarily.

Verify that the required services are running instead of assuming they are available.

Use PowerShell-compatible commands in the documentation.

Do not deploy the application or introduce Docker unless I explicitly request it.

---

### 7. MySQL database and administration

#### Planned database

- Name: `ecommerce_auth`
- Host: `localhost`
- Port: `3306`

The database name is a proposed name. Inspect the existing server before creating or modifying anything.

#### Access and permissions

1. Never assume or expose my MySQL root password.
2. Use secure local configuration for credentials.
3. Use a dedicated application database account with least-privilege permissions.
4. Keep real credentials out of Git.
5. Include placeholders in `.env.example` or equivalent configuration templates.
6. Ask me before creating databases or users, granting privileges, dropping tables, changing permissions, or modifying existing data.
7. Never delete existing database data without explicit approval.

#### Required core tables

**`users`**

- `id`
- `full_name`
- `email`
- `mobile_e164`
- `phone_region`
- `password_hash`
- `created_at`
- `updated_at`
- `password_changed_at`

**`user_sessions`**

- `id`
- `user_id`
- `jti`
- `created_at`
- `expires_at`
- `revoked_at`

**`password_recovery_challenges`**

- `id`
- `user_id`
- `otp_hash`
- `created_at`
- `expires_at`
- `attempt_count`
- `consumed_at`
- `delivery_channel`
- `recovery_token_hash`
- `recovery_expires_at`

Finalize the schema during discovery. Add appropriate data types, foreign keys, indexes, unique constraints, and any additional fields needed for correctness.

Use unique normalized emails and unique canonical international phone numbers.

Never store plaintext passwords, OTPs, raw JWTs, or raw recovery authorization tokens.

#### Database migrations

Use Flyway versioned SQL migrations in the backend repository.

Example:

`src/main/resources/db/migration/V1__create_users.sql`

Use subsequent migrations for session and recovery tables.

Configure Hibernate to validate the schema rather than automatically creating or altering tables at startup.

Inspect existing tables before applying migrations. Present destructive or potentially incompatible operations for approval.

Provide an ER diagram and explain the purpose of every table.

---

### 8. International phone number support

Support international phone numbers from multiple countries, not just India.

The registration and recovery interfaces must provide a suitable country selector with:

- Searchable country names.
- Country flags.
- International calling codes.
- Country-appropriate formatting.
- Country-specific validation.

For example, India uses `+91`, the United States uses `+1`, and the United Kingdom uses `+44`.

Do not assume that all countries have ten-digit phone numbers.

#### Validation and storage

1. Use a maintained international phone input library in React.
2. Use a maintained phone-number library in Java.
3. Validate on both frontend and backend.
4. Validate the complete number using the selected country or region.
5. Normalize valid phone numbers to E.164 format.
6. Store the canonical number in `mobile_e164`.
7. Store `phone_region` when appropriate.
8. Enforce uniqueness on the normalized phone number.
9. Support international numbers in login and password recovery.
10. Do not rely on simplistic regular expressions or calling codes alone.

Make country selection responsive, keyboard accessible, and usable with assistive technology.

---

### 9. Frontend design system

#### Design direction

Create a modern, clean, professional, responsive, Ecommerce type, Material Design-inspired interface.

Avoid excessive decoration, inconsistent spacing, generic unstyled forms, or unnecessary animations.

#### Light theme

- Primary: `#2563EB`
- Background: `#F8FAFC`
- Card: `#FFFFFF`
- Primary text: `#0F172A`
- Secondary text: `#64748B`
- Borders: `#E2E8F0`
- Success: `#16A34A`
- Error: `#DC2626`

#### Dark theme

- Primary: `#3B82F6`
- Background: `#0F172A`
- Card: `#1E293B`
- Primary text: `#F8FAFC`
- Secondary text: `#94A3B8`
- Borders: `#334155`
- Success: `#22C55E`
- Error: `#F87171`

Define colors as shared MUI theme tokens.

#### Typography and layout

- Use Inter or a comparable sans-serif font.
- Use a spacing system based approximately on 8 pixels.
- Use corner radii around 8–12 pixels.
- Use subtle shadows and clear visual hierarchy.
- Keep authentication cards approximately 420 pixels wide at most on desktop.
- Make layouts fluid on mobile.
- Avoid horizontal scrolling.
- Maintain accessible contrast and visible focus indicators.

#### Reusable components

Use reusable components where appropriate:

- `AuthLayout`
- `AuthCard`
- `FormInput`
- `PasswordInput`
- `PhoneNumberInput`
- `PrimaryButton`
- `ThemeToggle`
- `ProtectedRoute`
- `LoadingIndicator`
- `ErrorMessage`

All seven pages must share the same design system and theme behavior.

---

### 10. Required frontend pages and behavior

#### Page 1 — Registration

Route: `/register`

Title: `Create an account`

Fields:

- Full name.
- Email.
- International mobile number.
- Password.
- Confirm password.

Validation:

- Full name required, 3–100 characters after trimming.
- Valid normalized email.
- Valid international phone number.
- Password at least 8 characters with uppercase, lowercase, number, and special character.
- Password confirmation must match.
- Email and normalized phone number must be unique.

Requirements:

- Submit to `POST /api/reg`.
- Display validation and server errors.
- Prevent duplicate submissions.
- Show loading feedback.
- Show success feedback and link to login.
- Do not automatically log the user in after registration unless I approve it.

#### Page 2 — Login

Route: `/login`

Title: `Welcome back`

Fields:

- Email or international phone number.
- Password.

Requirements:

- Submit to `POST /api/login`.
- Accept email or international phone as the identifier.
- Show generic invalid-credentials messages.
- Handle loading and API errors.
- Navigate to Welcome Home after successful authentication.
- Link to registration.
- Include a **Forgot Password?** link to `/forgot-password`.

#### Page 3 — Welcome Home

Route: `/home`

Display:

**Heading:** `Welcome to Home Page!`

**Supporting text:** `You have successfully logged in.`

Include:

- Logout button.
- Optional authenticated user's name.
- Theme toggle.

Do not add products, cart, categories, or shopping functionality.

The route must be protected. Unauthenticated users should be redirected to login, and the backend must independently enforce authorization.

#### Page 4 — Forgot Password

Route: `/forgot-password`

Purpose: Initiate account recovery.

Provide:

- Email or international mobile identifier.
- Submit button.
- Back-to-login link.
- Loading and error feedback.

Requirements:

- Submit to `POST /api/forgot-password`.
- Accept either email or international phone.
- For phone recovery, provide the international country selector.
- Display a generic response that does not disclose whether the account exists.
- Explain that the next step is OTP verification without claiming delivery succeeded unless it actually did.
- Provide a way to continue to the OTP verification screen using the appropriate recovery flow.

Do not expose whether an account exists through the UI or API.

#### Page 5 — OTP Verification

Route: `/verify-otp`

Purpose: Verify the recovery OTP.

Provide:

- OTP input.
- Verify button.
- Resend OTP action, subject to the backend cooldown.
- Expiry or retry guidance.
- Navigation back to the recovery request page.
- Loading and error feedback.

Requirements:

- Submit to `POST /api/verify-otp`.
- Use a short-lived recovery flow.
- Never store the OTP in persistent browser storage.
- Do not trust frontend timers or attempt limits as security controls.
- Respect backend expiry, rate limits, and attempt limits.
- On successful verification, proceed to Reset Password with the recovery authorization handled securely.

The exact request DTO and delivery flow must be consistent with the backend API contract.

#### Page 6 — Reset Password

Route: `/reset-password`

Purpose: Set a new password after successful OTP verification.

Fields:

- New password.
- Confirm new password.

Requirements:

- Submit to `POST /api/reset-password`.
- Require valid recovery authorization.
- Validate password policy and confirmation.
- Show loading, success, and safe error messages.
- Reject expired or reused recovery authorization.
- After successful reset, navigate to login.
- Never automatically authenticate the user after resetting the password.

Do not expose recovery authorization in URLs or logs. Choose a secure short-lived transport consistent with the approved authentication and recovery design.

#### Page 7 — Change Password

Route: `/change-password`

Purpose: Allow an authenticated user to change their password.

Fields:

- Current password.
- New password.
- Confirm new password.

Requirements:

- Submit to `PUT /api/change-password`.
- Require authentication.
- Verify the current password.
- Validate the new password and confirmation.
- Show loading, success, and error feedback.
- Apply the documented session-revocation policy after a successful change.
- Provide a way to return to Welcome Home.

This page must not be accessible to unauthenticated users.

---

### 11. Frontend routing and authentication state

Use React Router for navigation.

Required routes:

- `/register`
- `/login`
- `/home`
- `/forgot-password`
- `/verify-otp`
- `/reset-password`
- `/change-password`

Redirect the root path `/` to an appropriate entry page, normally `/login`.

Protect `/home` and `/change-password`.

Handle authentication state consistently across page refreshes, navigation, login, logout, token expiry, and session revocation.

Do not treat a frontend-only boolean as proof of authentication.

Do not store bearer tokens in `localStorage` by default.

Implement the final authentication transport only after documenting the security tradeoffs and obtaining approval for the design.

---

### 12. Fixed backend API contract

Base URL: `http://localhost:8080`

**Do not change these paths or HTTP methods.**

| Method | Endpoint | Authentication |
|---|---|---|
| POST | `/api/reg` | Public |
| POST | `/api/login` | Public |
| POST | `/api/logout` | Authenticated |
| POST | `/api/forgot-password` | Public |
| POST | `/api/verify-otp` | Public |
| POST | `/api/reset-password` | Valid recovery authorization |
| PUT | `/api/change-password` | Authenticated |
| GET | `/api/me` | Authenticated |
| GET | `/api/home` | Authenticated |

#### Registration

`POST /api/reg`

Accept full name, email, international phone number, region, password, and password confirmation through a documented request DTO.

Validate all fields, normalize identifiers, reject duplicates, hash the password with BCrypt, and persist the account.

Return `201 Created` on success.

#### Login

`POST /api/login`

Example request:

```json id="0w5n0m"
{
  "identifier": "rahul@example.com",
  "password": "Example@123"
}
```

The identifier supports email or international phone.

On success, establish authentication using the approved token and session strategy.

Return generic invalid-credentials errors.

#### Logout

`POST /api/logout`

Revoke the current server-side session and return `204 No Content` on success.

#### Forgot password

`POST /api/forgot-password`

- Accept an email or international phone identifier.
- Apply rate limiting.
- Use a generic response to prevent account enumeration.
- Create a recovery challenge only when appropriate.
- Support the configured development delivery adapter.

#### Verify OTP

`POST /api/verify-otp`

- Validate the OTP.
- Enforce expiration and attempt limits.
- Consume the OTP after successful verification.
- Issue short-lived, reset-only recovery authorization.
- Prevent reuse.

#### Reset password

`POST /api/reset-password`

- Require valid recovery authorization.
- Enforce password policy and confirmation.
- Hash the new password.
- Consume recovery authorization once.
- Revoke all active sessions after successful reset.

#### Change password

`PUT /api/change-password`

- Require authentication.
- Verify the current password.
- Validate and hash the new password.
- Apply the documented session policy.

#### Current user and home

`GET /api/me` returns safe profile fields.

`GET /api/home` requires authentication and returns:

```json id="q3t21p"
{
  "message": "Welcome to Home Page"
}
```

Never return JPA entities directly as API responses.

#### Error responses

Use a consistent structure, such as:

```json id="j0k81x"
{
  "code": "VALIDATION_ERROR",
  "message": "Please correct the highlighted fields.",
  "fieldErrors": {
    "email": "Enter a valid email address."
  }
}
```

Use appropriate HTTP statuses, including `200`, `201`, `204`, `400`, `401`, `403`, `409`, and `429`.

Never expose stack traces, secrets, password hashes, OTPs, raw tokens, or internal security details.

---

### 13. Authentication and security architecture

Use BCrypt for password hashing.

Use JWT-based authentication with server-side session records to allow immediate session revocation.

Store session metadata or cookies, not raw JWTs.

The `user_sessions` table must support:

- Session identification.
- User association.
- Token/session identifier.
- Creation and expiry times.
- Revocation time.

The backend must reject expired and revoked sessions.

#### Authentication transport decision

Before implementation, document the tradeoffs and recommend one of these approaches:

1. HttpOnly cookie-based authentication with appropriate CSRF protection.
2. Bearer-token authentication with secure token handling.

Prefer HttpOnly cookies where appropriate. If bearer tokens are selected, do not persist them in `localStorage` by default.

The chosen approach must be implemented consistently across frontend, backend, CORS, logout, recovery, and tests.

#### Security controls

- Backend authentication and authorization.
- CORS restricted to the intended frontend origin.
- Login and recovery rate limiting.
- Secure session expiration and revocation.
- Backend request validation.
- Safe error handling.
- No secrets in Git.
- No plaintext passwords, OTPs, or recovery tokens.
- No sensitive data in logs.
- Transactional handling of related database changes.
- No frontend-only authorization.

---

### 14. OTP and password recovery security

Use the following as configurable initial defaults:

- OTP length: 6 digits.
- Expiry: 5 minutes.
- Maximum attempts: 5.
- Resend cooldown: 60 seconds.

Implement:

- Secure OTP generation.
- Hashed OTP storage.
- Rate limiting.
- Expiration checks.
- Attempt limits.
- Single-use OTP verification.
- Short-lived recovery authorization.
- Single-use recovery authorization.
- Session revocation after password reset.

Provide a development-only local OTP delivery adapter.

Do not claim actual SMS or email delivery without a configured provider.

Do not expose OTPs or recovery tokens in ordinary API responses or logs.

Document how local development recovery works. If an approved development-only mechanism is needed to inspect a generated OTP, isolate it from production configuration and prevent it from being enabled accidentally in production.

---

### 15. Frontend and backend integration

Use:

`VITE_API_BASE_URL=http://localhost:8080/api`

Keep frontend and backend independently runnable.

Ensure all request DTOs, response DTOs, error formats, authentication handling, and endpoint paths match exactly.

Implement real API calls; do not simulate successful registration or login in the completed application.

Handle:

- Loading states.
- Validation errors.
- Server errors.
- Duplicate registration.
- Invalid credentials.
- OTP expiry.
- OTP attempt limits.
- Recovery authorization expiry.
- Expired authentication.
- Session revocation.
- Logout.
- Successful password changes.

Configure CORS for the actual local frontend origin and the approved authentication transport.

---

### 16. Testing requirements

#### Frontend tests

Test all seven pages and their primary user flows:

- Registration validation and submission.
- Login using email and phone.
- Country search, flags, calling codes, formatting, and validation.
- Forgot-password request.
- OTP verification.
- OTP resend behavior and error handling.
- Reset-password validation and submission.
- Change-password validation and submission.
- Protected routes.
- Login and logout.
- Theme switching.
- API success and error handling.
- Loading states and duplicate-submission prevention.
- Responsive layout and accessibility.
- Expired or revoked authentication.
- Safe recovery authorization handling.

Use Vitest and React Testing Library.

#### Backend tests

Test:

- Registration success and validation failures.
- Duplicate email and phone numbers.
- BCrypt password hashing.
- International phone validation and E.164 normalization.
- Login using email and phone.
- Generic invalid-credentials responses.
- Protected endpoints.
- JWT/session validation and revocation.
- Logout.
- Session expiry.
- Forgot-password account-enumeration protection.
- OTP expiry, attempts, rate limiting, and reuse.
- Recovery authorization expiry and reuse.
- Password reset.
- Old-password rejection after reset.
- Session revocation after reset.
- Change-password behavior.
- Database migrations and constraints.
- Safe errors and authorization.

Use JUnit, Spring Boot Test, and MockMvc where appropriate.

#### Integration tests

Verify:

- Frontend/backend DTO compatibility.
- CORS.
- Authentication transport.
- Registration and login against MySQL.
- Protected home access.
- Logout revocation.
- Forgot-password and OTP recovery flow.
- Password reset session invalidation.
- Change-password authorization.
- Flyway migrations.

Run tests and report actual results. Never fabricate test results.

---

### 17. Documentation and deliverables

Deliver:

1. Frontend source code.
2. Backend source code.
3. Seven frontend routes/pages.
4. Backend authentication and recovery APIs.
5. Flyway migrations.
6. `.gitignore` files.
7. `.env.example` or equivalent safe configuration templates.
8. Separate README files.
9. Windows PowerShell setup and startup instructions.
10. OpenAPI/Swagger API documentation.
11. Postman collection covering all required endpoints.
12. Database schema documentation and ER diagram.
13. Frontend and backend automated tests.
14. Requirements traceability matrix.
15. Troubleshooting guide.
16. Final handover report.

Document how to configure local variables, start MySQL, run migrations, start the backend, start the frontend, test the APIs, and run the automated tests.

Never include real credentials in documentation or source control.

---

### 18. Implementation phases

**Phase 1 — Discovery and planning**

Inspect both repositories and the local environment. Verify MySQL, propose architecture and schema, document authentication choices, and present the implementation plan. Wait for my approval.

**Phase 2 — Environment and repository setup**

Prepare existing repositories, configurations, dependencies, and local setup documentation.

**Phase 3 — Backend registration and database**

Implement the user entity, repository, validation, phone normalization, BCrypt hashing, registration API, Flyway migrations, and tests.

**Phase 4 — Backend authentication**

Implement login, JWT/session tracking, protected `/api/me` and `/api/home`, logout, and tests.

**Phase 5 — Frontend foundation**

Set up React, JavaScript, Vite, MUI themes, shared components, routing, API services, and error handling.

**Phase 6 — Registration, login, and home frontend**

Implement and integrate the first three pages.

**Phase 7 — Recovery frontend and backend**

Implement forgot-password, OTP verification, reset-password, and change-password APIs and screens. Apply security controls and tests.

**Phase 8 — Full integration**

Verify all seven pages, all required APIs, authentication transport, CORS, international phone support, and MySQL persistence.

**Phase 9 — Final verification and handover**

Run tests, verify migrations, complete documentation, and report remaining issues.

At the end of every phase, report:

- What was completed.
- Files created or changed.
- Commands executed.
- Actual test results.
- Remaining issues.
- Next proposed phase.

Do not skip phases or silently change the scope.

---

### 19. Definition of done

The project is complete when:

- Both existing GitHub repositories are used correctly.
- Frontend runs at `http://localhost:5173`.
- Backend runs at `http://localhost:8080`.
- Backend connects to the existing local MySQL installation.
- Database migrations work.
- Registration and login work against the real backend.
- International phone numbers are validated and normalized correctly.
- Passwords are securely hashed.
- JWT/session tracking and revocation work.
- Home and change-password routes are protected.
- Logout invalidates the current session.
- Forgot-password, OTP verification, reset-password, and change-password work end to end.
- All seven frontend pages are implemented.
- Light and dark themes work consistently.
- Automated tests have been run and accurately reported.
- API documentation, database documentation, and setup instructions are complete.
- No secrets have been committed.
- No unauthorized destructive database operations, GitHub pushes, or deployments have occurred.

---

### 20. Final instructions

Prioritize security, correctness, readable code, and maintainability.

Do not invent environment details or claim actions you did not perform.

Do not assume existing repositories or databases are empty.

Ask for approval before destructive changes, administrative database operations, repository pushes, deployment, or significant architectural changes.

Explain important decisions in beginner-friendly language.

**Your immediate task is Phase 1 only. Inspect the two supplied repositories and available local environment, then present the discovery report and proposed implementation plan. Do not begin major coding until I approve the report.**












//**

## Add-on Instructions — Testing Override and Project Handover Documentation

These instructions override only the original prompt's automated testing requirements and add a mandatory project handover document. All other requirements, technologies, features, security rules, implementation phases, and instructions in the original prompt remain unchanged.

### 1. Automated Testing — Deferred for Now

For the current development phase, I do not want automated testing implemented.

- Do not create automated frontend tests, backend tests, or integration tests.
- Do not set up testing frameworks or test infrastructure specifically for this project at this stage.
- I will manually check the application's functionality.
- You may perform basic development checks, such as compiling the backend, building the frontend, checking database migrations, and confirming that the applications start successfully.
- Do not remove any functional requirements, validation, security controls, or error handling because automated testing is deferred.
- Do not claim that automated tests passed or were executed when they were not.

This instruction overrides the original Section 16 and any other instructions that require creating or running automated tests for the current phase. All other original requirements remain in effect.

### 2. Mandatory Final Project Handover Document

Create a comprehensive document named `PROJECT_CONTEXT.md` in an appropriate location within the existing project repositories. Do not create another repository for it.

The purpose of this document is to allow me to give it to the same AI agent or a different AI agent in the future so it can understand the existing project and continue development without requiring me to explain everything again.

Document the actual implementation, including:

- Project purpose, scope, and current implementation status.
- Technologies, frameworks, libraries, and relevant versions.
- Frontend and backend repository structures, important files, and their purposes.
- Application architecture and how the frontend, backend, APIs, and database interact.
- Implemented features, their workflows, and incomplete or deferred features.
- Database schema, tables, relationships, constraints, and migrations.
- API endpoints, HTTP methods, request and response formats, and authentication requirements.
- Authentication, JWT, session management, password hashing, security controls, and password recovery implementation.
- Frontend routes, components, form validation, API integration, and authentication state.
- Environment variables and configuration, using placeholders instead of real secrets.
- Exact local setup and startup instructions for Windows.
- Commands required to run the frontend, backend, and database migrations.
- Important implementation decisions, known issues, limitations, and unverified behavior.
- Recommended next steps for future development.

Keep this document updated as implementation progresses. Describe what actually exists in the code; clearly distinguish implemented functionality from planned functionality.

At the end of development, ensure the document is sufficiently detailed for another AI agent to read it, inspect the repositories, understand the current architecture, identify what has already been completed, and implement new features without unnecessarily rebuilding or breaking existing functionality.

### 3. Instructions for Future AI Agents

Include a section in `PROJECT_CONTEXT.md` instructing future agents to:

1. Read the document before modifying the project.
2. Inspect the existing repositories, code, configuration, and Git status.
3. Preserve existing work and follow the current architecture.
4. Avoid duplicating already implemented functionality.
5. Verify the actual code rather than relying solely on the document.
6. Update the document whenever implementation changes.
7. Implement only the features requested for the next development step.
8. Avoid adding automated testing unless I explicitly request it.

### 4. Final Handover

At the end, report the completed work, files changed, setup and startup instructions, remaining work, and the exact location of `PROJECT_CONTEXT.md`. Clearly state which basic checks were performed and which functionality remains unverified.

**Important:** Continue following my original prompt for everything except the automated testing requirements overridden above and the additional documentation requirement introduced here.
**//
## Current authorized implementation status (2026-10-20)

The user's latest task instruction supersedes the original Phase 1 approval gate for this authentication milestone and explicitly authorizes implementation, automated tests, and verification. The earlier testing-deferral add-on remains historical guidance only for this authorized task; 10 frontend tests and 7 backend tests have been implemented and passed. The current design preference is light mode only, and no shopping features were added. See the root `PROJECT_CONTEXT.md` and backend `docs/handover/VERIFIED_GAP_ASSESSMENT.md` for exact verified behavior and remaining limits. Browser reset submission is pending user handoff; MySQL migration/persistence and production deployment were not verified.
