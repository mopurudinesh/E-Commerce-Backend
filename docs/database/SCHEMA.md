# Authentication database schema

The schema is owned by Flyway migration src/main/resources/db/migration/V1__create_auth_schema.sql. Hibernate uses ddl-auto: validate. Application startup applies unapplied Flyway migrations to the configured database, so verify SPRING_DATASOURCE_URL before starting the normal local profile. Automated tests use a separate in-memory H2 database with Flyway disabled and do not touch MySQL.

| Table | Main columns | Constraints and role |
|---|---|---|
| users | id, full_name, email, mobile_e164, phone_region, password_hash, timestamps, password_changed_at | Unique email and E.164 phone. Passwords are BCrypt hashes. |
| user_sessions | id, user_id, jti, created_at, expires_at, revoked_at | Unique JTI; foreign key to users with ON DELETE CASCADE. The row is required for a JWT to authenticate. |
| password_recovery_challenges | id, user_id, otp_hash, creation/expiry times, attempt count, consumed time, delivery channel, recovery-token hash and expiry | Foreign key to users with ON DELETE CASCADE. OTP and reset authorization are stored as hashes; the challenge is deleted after successful reset. |
| flyway_schema_history | Flyway-managed metadata | Created by Flyway when migration runs. |

The user-to-session and user-to-recovery relationships are one-to-many. Deleting a user cascades to related sessions and recovery challenges. There is no refresh-token table or commerce data in this milestone.

This documentation describes the migration source. Tests verify persistence against H2; they do not prove this migration against the user's MySQL server. Do not run a migration against a normal database without checking the target URL and schema first.
