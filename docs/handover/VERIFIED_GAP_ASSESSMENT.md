# Verified gap assessment and completion checklist

Checked against source, automated tests, and the local browser run on 2026-10-20. “Verified” is limited to the named test or observed isolated browser flow; it does not imply MySQL or production verification.

| Requirement | Status | Evidence / remaining limit |
|---|---|---|
| Distinct protected Change Password route | Implemented and verified | Frontend route test and manual authenticated navigation; signed-out protection is covered by the frontend test. |
| Change Password validation, loading/errors/success, PUT call and auth clear | Implemented and verified | Frontend UI tests; wrong-current-password manually observed; backend test verifies successful change and all-session revocation. |
| International phone input for registration/recovery | Implemented and verified | Maintained international input used in frontend tests and isolated browser registration/recovery; backend validates/normalizes E.164. |
| Duplicate email/phone, confirmation and registration persistence | Implemented and verified | Backend H2 API/JPA tests. MySQL persistence remains unverified. |
| Email/phone login and generic credential errors | Implemented and verified | Backend integration tests; browser verified email and international phone login. |
| Cookie/session creation, protection, logout, expiry and revocation | Implemented and verified | Backend integration tests and manual browser login/logout/protected redirect. |
| CSRF and credentialed CORS | Implemented and verified | Backend tests check missing token rejection, token endpoint/cookie, valid token, and allowed/rejected origins. Browser registration/login exercised Axios token flow. |
| Recovery generic response, OTP expiry/attempts/rate limit/reuse | Implemented and verified | Backend integration/unit tests; browser requested recovery and verified local OTP. Browser reset submission remains pending handoff. |
| Reset-password success and session invalidation | Implemented and verified in backend | H2 API/JPA tests cover reset and invalidation. Live browser is on Reset Password screen awaiting user password entry/submission; no browser success claim. |
| API/CORS/cookie/H2 persistence compatibility | Implemented and partially verified | MockMvc and H2 JPA coverage. Actual MySQL migration and server persistence were not verified. |
| Postman, schema/ER, traceability, troubleshooting and setup docs | Implemented | Backend `docs/api`, `docs/database`, `docs/handover`, README and context files. Postman JSON parsed successfully and contains 10 requests. |
| Light-only design; no shopping features added | Implemented | Light palette retained; this change added no shopping features. |
| Full manual browser journey through successful reset | Incomplete pending user handoff | Reset screen is open in the isolated H2 browser. User must enter and submit a new test-only password under the computer-use handoff policy. Automated backend reset success is verified. |
| Production readiness | Intentionally out of scope | MySQL integration, real email/SMS provider, distributed rate limiting, HTTPS/domain policy and deployment review remain outstanding. |

Tests and browser verification used isolated H2. An earlier startup attempt connected to configured MySQL and Flyway reported V1 already up to date; no migration or application data change was made. No `.env` values were read.
