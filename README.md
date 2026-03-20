# CSFC Auth Service

Spring Boot authentication/authorization service for CSFC. Handles registration/login/JWT, role/permission seeding, and customer profile/admin/internal bridge APIs exposed behind the gateway prefix `/api/auth-service`.

## Quick Start
```powershell
./mvnw.cmd clean package
java -jar target/*.jar
```

## Required Environment
Reads from `.env` (imported by `application.yml`) or process env vars:
- `SERVER_PORT`
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (PostgreSQL)
- `JWT_SECRET` (required)
- `JWT_ACCESS_EXPIRATION` (ms, default 900000)
- `JWT_REFRESH_EXPIRATION` (ms, default 604800000)
- `MAIL_USERNAME`, `MAIL_PASSWORD`
- Admin bootstrap: `ADMIN_INIT_ENABLED` (default false), `ADMIN_INIT_EMAIL`, `ADMIN_INIT_PASSWORD`, `ADMIN_INIT_NAME` (default "System Admin"), `ADMIN_INIT_ROLE` (default "ADMIN")
- Eureka (optional): `EUREKA_CLIENT_ENABLED`, `EUREKA_URL`

## Seeding Behavior
`common/config/DataInitializer.java` seeds if missing:
- Roles: `ADMIN`, `CUSTOMER`, `STAFF`
- Permissions: `PERMISSION_ASSIGN`, `PERMISSION_VIEW`, `ROLE_CREATE`, `ROLE_UPDATE`, `ROLE_VIEW`, `ROLE_DELETE`, `USER_DELETE`, `USER_UPDATE_STATUS`, `USER_CREATE`, `USER_READ_SELF`, `CUSTOMER_PROFILE_UPDATE_SELF`, `CUSTOMER_PROFILE_LIST`, `CUSTOMER_PROFILE_VIEW`, `CUSTOMER_PROFILE_UPDATE`, `CUSTOMER_PROFILE_STATUS_UPDATE`, `CUSTOMER_PROFILE_LOCK`, `CUSTOMER_PROFILE_UNLOCK`, `CUSTOMER_SEARCH`, `CUSTOMER_ACTIVITY_VIEW`, `INTERNAL_CUSTOMER_READ`, `INTERNAL_CUSTOMER_WRITE`, `CUSTOMER_AUDIT_VIEW`
- Role → permissions:
  - `ADMIN`: all above
  - `STAFF`: `USER_READ_SELF`, `CUSTOMER_PROFILE_VIEW`
  - `CUSTOMER`: `USER_READ_SELF`, `CUSTOMER_PROFILE_UPDATE_SELF`
- Admin seeding (if enabled and email/password provided) creates an admin with role `ADMIN_INIT_ROLE`.

## Key Endpoints (gateway prefix `/api/auth-service`)
`/auth`
- `POST /register` (public)
- `POST /login` (public)
- `POST /refresh` (public)
- `POST /forgot-password` (public)
- `POST /reset-password` (public)

`/users`
- `GET /me` (`USER_READ_SELF`)
- `DELETE /{id}` (`USER_DELETE`)
- `PATCH /{id}/deactivate` (`USER_UPDATE_STATUS`)
- `POST /create-account` (`USER_CREATE`)

`/roles`
- `POST /create` (`ROLE_CREATE`)
- `POST /update` (`ROLE_UPDATE`)
- `GET /` (`ROLE_VIEW`)
- `POST /delete/{id}` (`ROLE_DELETE`)

`/admin/roles`
- `POST /{roleId}/permissions?permissionName=...` (`PERMISSION_ASSIGN`)
- `GET /permissions` (`PERMISSION_VIEW`)
- `GET /{roleId}/permissions` (`PERMISSION_VIEW`)

`/customers` (customer self)
- `GET /me/details` (`USER_READ_SELF`)
- `PUT /me/details` (`CUSTOMER_PROFILE_UPDATE_SELF`)

`/admin/customers`
- `GET /all-profile` (`CUSTOMER_PROFILE_LIST`)
- `GET /{userId}/profile` (`CUSTOMER_PROFILE_VIEW`)
- `PUT /{userId}/profile` (`CUSTOMER_PROFILE_UPDATE`)
- `PATCH /{userId}/profile/status` (`CUSTOMER_PROFILE_STATUS_UPDATE`)
- `PATCH /{userId}/profile/lock` (`CUSTOMER_PROFILE_LOCK`)
- `PATCH /{userId}/profile/unlock` (`CUSTOMER_PROFILE_UNLOCK`)
- `POST /search` (`CUSTOMER_SEARCH`)
- `GET /{userId}/activity` (`CUSTOMER_ACTIVITY_VIEW`)
- `GET /{userId}/audit` (`CUSTOMER_AUDIT_VIEW`)

`/internal/customers`
- `GET /{userId}/details` (`INTERNAL_CUSTOMER_READ`)
- `PUT /{userId}/details` (`INTERNAL_CUSTOMER_WRITE`)
- `POST /bridge` (`INTERNAL_CUSTOMER_WRITE`)

`/public/rbp`
- `POST /register` (role/permission bootstrap for peer services)

## Security
- Spring Security with JWT and custom `AuthorizationFilter`.
- Authorities derived from `CustomerUserDetails`: `ROLE_<NAME>` plus all permissions on the role.
- `isEnabled` checks `CustomerStatus.ACTIVE`.

## Notes / Risks
- `createUserWithRoleByAdmin` still uses a temp password `Demo@123` (consider rotating/generating).
- Ensure DB is reachable and `JWT_SECRET` is set; startup will fail otherwise.
- New audit logging writes to `customer_audit_logs`; ensure the table exists/migrated.

## Verification
```powershell
./mvnw.cmd clean package
```

```env
SERVER_PORT=8080
DB_URL=jdbc:postgresql://localhost:5432/csfc_auth
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=change_me
ADMIN_INIT_ENABLED=true
ADMIN_INIT_EMAIL=admin@example.com
ADMIN_INIT_PASSWORD=StrongPass123!
```
