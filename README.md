# CSFC Auth Service

Spring Boot authentication and authorization service for CSFC. Provides registration/login, JWT issuance, user/role/permission management, and seeding for initial roles/permissions.

## Quick Start
```powershell
# Build
./mvnw.cmd clean package

# Run (after setting env vars or .env)
java -jar target/*.jar
```

## Required Environment
The app reads from `.env` (imported by `application.yml`) or process env vars.
- `SERVER_PORT`
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (PostgreSQL)
- `JWT_SECRET` (required)
- `JWT_ACCESS_EXPIRATION` (ms, default 900000)
- `JWT_REFRESH_EXPIRATION` (ms, default 604800000)
- `MAIL_USERNAME`, `MAIL_PASSWORD`
- Admin bootstrap: `ADMIN_INIT_ENABLED` (default false), `ADMIN_INIT_EMAIL`, `ADMIN_INIT_PASSWORD`, `ADMIN_INIT_NAME` (default "System Admin"), `ADMIN_INIT_ROLE` (default "ADMIN")
- Eureka (optional): `EUREKA_CLIENT_ENABLED`, `EUREKA_URL`

## Seeding Behavior
Defined in `src/main/java/service/CSFC/CSFC_auth_service/common/config/DataInitializer.java`.
- Roles created if missing: `ADMIN`, `CUSTOMER`, `STAFF`
- Permissions created if missing: `PERMISSION_ASSIGN`, `PERMISSION_VIEW`, `ROLE_CREATE`, `ROLE_UPDATE`, `ROLE_VIEW`, `ROLE_DELETE`, `USER_DELETE`, `USER_UPDATE_STATUS`, `USER_CREATE`, `USER_READ_SELF`
- Role → permissions:
  - `ADMIN`: all above
  - `STAFF`: `USER_READ_SELF`
  - `CUSTOMER`: `USER_READ_SELF`
- Admin user seeding (if `ADMIN_INIT_ENABLED=true` and email/password provided): creates an admin account with role `ADMIN_INIT_ROLE` (default `ADMIN`).

## Key Endpoints
`/api/auth-service/auth`
- `POST /register` (public)
- `POST /login` (public)
- `POST /refresh` (public)
- `POST /forgot-password` (public)
- `POST /reset-password` (public)

`/api/auth-service/users`
- `GET /me` (`USER_READ_SELF`)
- `DELETE /{id}` (`USER_DELETE`)
- `PATCH /{id}/deactivate` (`USER_UPDATE_STATUS`)
- `POST /create-account` (`USER_CREATE`)

`/api/auth-service/roles`
- `POST /create` (`ROLE_CREATE`)
- `POST /update` (`ROLE_UPDATE`)
- `GET /` (`ROLE_VIEW`)
- `POST /delete/{id}` (`ROLE_DELETE`)

`/api/auth-service/admin/roles`
- `POST /{roleId}/permissions?permissionName=...` (`PERMISSION_ASSIGN`)
- `GET /permissions` (`PERMISSION_VIEW`)
- `GET /{roleId}/permissions` (`PERMISSION_VIEW`)

## Security
- Spring Security with JWT and custom `AuthorizationFilter`.
- Authorities from `CustomerUserDetails`: `ROLE_<NAME>` plus each permission.
- `isEnabled` now checks `CustomerStatus.ACTIVE`.

## Known Risks / Checks
- Ensure `JWT_SECRET` is set; startup will fail otherwise.
- DB connection must be valid (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).
- Admin seeding requires `ADMIN_INIT_ENABLED=true` and admin email/password.
- Hard-coded temp password in `createUserWithRoleByAdmin` is `Demo@123`; consider rotating or providing a generated password.

## Verification
- Build: `./mvnw.cmd clean package`
- Adjust `.env` as needed; example:
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

