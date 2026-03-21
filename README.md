# CSFC Auth Service

AuthN/AuthZ service for CSFC exposed behind the gateway prefix `/api/auth-service`. Provides login/refresh/logout, role/permission management, user + customer profile management, internal bridges for other services, password reset (OTP + email), audit logging, and role/permission bootstrap for peer services.

## What the service does
- Auth flows: register, login, refresh, logout (JWT + refresh token rotation), password reset via email OTP + reset token.
- User management: self profile read; admin create/deactivate/delete accounts; admin updates user roles.
- Roles & permissions: create/update/delete roles, list all, assign permissions to roles; `AuthorizationFilter` builds authorities per user.
- Customer profile: self read/update; admin list/search/view/update/lock/unlock/set status; admin pulls customer activity (via `OrderClient`) and audit logs.
- Internal bridge: CRUD-style endpoints for other services to read/update/create customers.
- RBP bootstrap: `/public/rbp/register` lets other services seed roles/permissions in this service.
- Discovery & docs: optional Eureka registration; Swagger/OpenAPI available at `/swagger-ui.html`.

## Architecture (high level)
- Spring Boot 4 (Java 17), Spring Security with method security, JWT (jjwt), JPA/Hibernate, OpenFeign, Spring Mail, Redis (token/OTP store), Lombok.
- Persistence: JPA entities (`Users`, `Roles`, `Permissions`, `CustomerAuditLog`, etc.) with a `BaseEntity` timestamp base class.
- Security: `AuthorizationFilter` validates JWT, injects `CustomerUserDetails`; CORS is open (`*`).
- Feign clients: `OrderClient` and `FranchiseClient` call external services via URLs from `application.config.*`.
- Templates: Thymeleaf reset pages (`templates/reset-password.html`, `reset-success.html`).

## Data/seed behavior
- `DataInitializer` seeds default roles (`ADMIN`, `CUSTOMER`, `STAFF`) and the full permission set, then grants role→permission mappings.
- Optional admin seeding via env: `ADMIN_INIT_ENABLED`, `ADMIN_INIT_EMAIL`, `ADMIN_INIT_PASSWORD`, `ADMIN_INIT_NAME`, `ADMIN_INIT_ROLE` (defaults to `ADMIN`).

## Key endpoints (all prefixed with `/api/auth-service`)
- `/auth`: `POST /register`, `POST /login`, `POST /refresh`, `POST /forgot-password`, `POST /reset-password`.
- `/password`: `POST /forgot`, `POST /verify-otp`, `POST /reset` (OTP + reset token flow stored in Redis).
- `/users`: `GET /me`, `DELETE /{id}`, `PATCH /{id}/deactivate`, `POST /create-account`, `PATCH /{id}/role`.
- `/roles`: `POST /create`, `POST /update`, `GET /`, `POST /delete/{id}`.
- `/admin/roles`: `POST /{roleId}/permissions`, `GET /permissions`, `GET /{roleId}/permissions`.
- `/customers`: `GET /me/details`, `PUT /me/details`.
- `/admin/customers`: `GET /all-profile`, `GET /{userId}/profile`, `PUT /{userId}/profile`, `PATCH /{userId}/profile/status`, `PATCH /{userId}/profile/lock`, `PATCH /{userId}/profile/unlock`, `POST /search`, `GET /{userId}/activity`, `GET /{userId}/audit`.
- `/internal/customers`: `GET /{userId}/details`, `PUT /{userId}/details`, `POST /bridge`.
- `/public/rbp`: `POST /register`.

## Configuration
- Env is loaded from `.env` (via `spring.config.import`) or process env vars.
- Database: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`; driver defaults to PostgreSQL (`org.postgresql.Driver`, dialect `PostgreSQLDialect`). If you point to MySQL, switch the driver/dialect in `application.yml` accordingly.
- JWT: `JWT_SECRET` (required), `JWT_ACCESS_EXPIRATION` (ms), `JWT_REFRESH_EXPIRATION` (ms).
- Mail: `MAIL_USERNAME`, `MAIL_PASSWORD` (Gmail SMTP, port 587).
- Admin seed: `ADMIN_INIT_*` vars above.
- Service URLs: `ORDER_SERVICE_URL`, `FRANCHISE_SERVICE_URL`.
- Eureka (optional): `EUREKA_CLIENT_ENABLED`, `EUREKA_URL`, `RENDER_EXTERNAL_HOSTNAME`.
- Reset URL base: `RESET_PASSWORD_BASE_URL` used to build reset links in emails.
- Redis: `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` (see the Redis note below).

## Redis expectation (important)
The app actively uses Redis for:
- Refresh-token/session storage and blacklist (`AuthenticationServiceImp`).
- OTP + reset-token storage and cooldowns for forgot/reset password (`PasswordServiceImp`).

If Redis is unavailable, login refresh/rotation, logout, and password-reset flows will throw errors because `RedisTemplate` is required. You have two options:
1) Run Redis (recommended):
   ```powershell
   docker run --name csfc-redis -p 6379:6379 -d redis:7-alpine
   ```
   Then set `REDIS_HOST=localhost`, `REDIS_PORT=6379` (and password if you add one).
2) Skip Redis (not supported yet): you would need to add an alternative token/OTP store (e.g., DB-backed) or disable the features; out-of-the-box the app expects Redis to be reachable.

## Running locally
1) Create a `.env` with your values (DB, JWT secret, mail, Redis, Eureka). Example:
   ```env
   SERVER_PORT=8081
   DB_URL=jdbc:postgresql://localhost:5432/csfc_auth
   DB_USERNAME=postgres
   DB_PASSWORD=postgres
   JWT_SECRET=change_me
   ORDER_SERVICE_URL=http://localhost:8080
   FRANCHISE_SERVICE_URL=http://localhost:8080
   ADMIN_INIT_ENABLED=true
   ADMIN_INIT_EMAIL=admin@example.com
   ADMIN_INIT_PASSWORD=StrongPass123!
   REDIS_HOST=localhost
   REDIS_PORT=6379
   RESET_PASSWORD_BASE_URL=https://auth-service.example.com
   ```
2) Build and run:
   ```powershell
   ./mvnw.cmd clean package
   java -jar target/CSFC-auth-service-0.0.1-SNAPSHOT.jar
   ```

## Observability & docs
- Actuator: `/actuator/health`, `/actuator/info` (see `application.yml` exposure list).
- Swagger UI: `/swagger-ui.html`, OpenAPI: `/v3/api-docs`.

## Notes / risks to keep in mind
- Ensure Redis is running before using login/refresh/logout/password flows.
- Align `DB_URL` and the JDBC driver/dialect in `application.yml` (currently set for PostgreSQL; `.env` sample uses MySQL).
- Mail must be configured for OTP/reset emails to be delivered; otherwise reset will fail when sending.
- CORS is wide open by default; tighten in production if needed.
