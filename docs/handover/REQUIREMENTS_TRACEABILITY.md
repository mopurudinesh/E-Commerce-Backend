# Requirements traceability

| Requirement | Implementation | Verification |
|---|---|---|
| Registration validation, duplicate identifiers, password confirmation | AuthService, RegisterRequest, PhoneValidationService | AuthIntegrationTest.registrationNormalizesInternationalNumberPersistsHashedPasswordAndRejectsDuplicates |
| International registration and recovery numbers | PhoneNumberInput, RegisterPage, ForgotPasswordPage, PhoneValidationService | Frontend international phone flow test; backend registration and recovery tests |
| Email and phone login, generic failure | AuthService, AuthController | AuthIntegrationTest.loginByEmailAndPhoneCreatesHttpOnlyCookieAndProtectedSessionCanLogout |
| Cookie session, protected APIs, logout, expiry and revocation | AuthService, JwtAuthenticationFilter, UserSession | Backend login/logout, protected resource, session expiry, password reset/change tests |
| CSRF, CORS, SPA request compatibility | SecurityConfig, AuthController csrf endpoint, Axios request interceptor | Backend CSRF and CORS tests; browser registration/login exercised Axios token-cookie-header round trip |
| Forgot-password enumeration protection | PasswordRecoveryService, AuthController | Backend recovery and generic response tests |
| OTP expiry, attempt limit, reuse, cooldown, rate limits | PasswordRecoveryService, InMemoryRateLimiter, recovery entities | Backend attempts, expiry, one-use tests and limiter unit test; cooldown code inspection |
| Reset token expiry/reuse and session invalidation | PasswordRecoveryService | Backend expiry, reuse, password change, and session invalidation tests |
| Change Password form/route, loading/errors/success | ChangePasswordPage, ProtectedRoute, Navbar | authFlows.test.jsx Change Password and protected route cases |
| OTP and reset flow state handling | VerifyOtpPage, ResetPasswordPage | authFlows.test.jsx verifies token remains in router state, not URL |
| Database schema and relationships | Flyway V1 migration | H2 integration persistence. MySQL migration is not run by automated tests. |
| Manual end-to-end journey | Browser with H2 test profile | Registration, duplicate rejection, email/phone login, logout, signed-out redirect, Change Password navigation/wrong-current-password rejection, recovery request and OTP verification were observed. Browser reset submission remains pending user handoff. |

Test configuration is src/test/resources/application-test.yml and uses only jdbc:h2:mem:auth_test. It disables Flyway and never falls back to a MySQL URL.

The local OTP delivery adapter logs the development code in the backend console; the frontend does not display the OTP. The successful reset and session invalidation are covered by backend integration tests, but the live browser reset form has not been submitted.
