# INT216D Car Wash — Backend

Mobile & Bay car wash booking platform. Spring Boot microservices backed by PostgreSQL, Redis, and Kafka.

## Modules (Task 1–3, 6 complete)

| Module | Port | Responsibility |
|---|---|---|
| `api-gateway` | 8080 | Single entry point; routes `/api/v1/**` to the right service |
| `auth-service` | 8081 | Registration, email OTP verification, login, JWT, refresh |
| `client-service` | 8082 | Client profile, vehicles, addresses, debit-order mandate |
| `booking-service` | 8083 | Booking slots, membership plans, credit management (Tasks 5-6) |
| `common` | — | Shared JWT service, security filter, exceptions, DTOs |

**Note**: Tasks 4 & 5 (Catalogue & Booking) pending from your team. Task 6 (Membership) now complete.

## Quick Start

```bash
# 1. Start infrastructure (Postgres, Redis, Kafka, MailHog)
docker compose up -d postgres redis mailhog

# 2. Build everything
./mvnw clean install -DskipTests      # or: mvn clean install -DskipTests

# 3. Run each service in its own terminal
mvn -pl auth-service    spring-boot:run
mvn -pl client-service  spring-boot:run
mvn -pl booking-service spring-boot:run
mvn -pl api-gateway     spring-boot:run
```

All services use the same base URL shape described in the project doc:
`http://localhost:8080/api/v1/...`

## Environment

Create `.env` and set values as needed. Key settings:

| Variable | Default (dev) | Notes |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/int216d_carwash` | Single DB, four schemas |
| `JWT_SECRET` | `CHANGE_ME_TO_A_LONG_RANDOM_SECRET_MIN_32_BYTES` | **Change in production.** Min 32 chars. |
| `MAIL_HOST` / `MAIL_PORT` | `localhost` / `1025` | SMTP server host and port |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | empty | SMTP credentials from your mail provider |
| `MAIL_FROM_ADDRESS` | `noreply@int216d.co.za` | Sender address for no-reply mails |
| `MAIL_FROM_NAME` / `MAIL_PROJECT_NAME` | `INT216D Smart Car Wash` | Branded sender display name and project identity |

## Endpoints Live After Task 1-3

### Auth (`/api/v1/auth`) — public
- `POST /register` → `{ email, password }`
- `POST /verify-email` → `{ email, otp }`
- `POST /login` → `{ email, password }` — sets HttpOnly refresh cookie
- `POST /refresh` → reads refresh cookie, returns fresh access token

### Client (`/api/v1/clients/me`) — `ROLE_CLIENT`
- `GET /` — profile
- `POST /` — create profile (first time)
- `PUT /` — update profile
- `GET|POST /vehicles`, `DELETE /vehicles/{id}`
- `GET|POST /addresses`
- `POST /mandate`, `GET /mandate`

## Schema Layout

```
int216d_carwash (db)
├── auth_schema         owned by auth-service      (users, email_otps, refresh_tokens)
├── client_schema       owned by client-service    (clients, vehicles, addresses, mandates)
└── booking_schema      owned by booking-service   (membership_plans, memberships, credit_logs, bookings, slots)
```

Each service runs Flyway only inside its own schema. **No service ever reaches across schemas.**

## SDLC Progress

- [x] Task 1 — Project setup + Docker Compose + DB schemas
- [x] Task 2 — Auth service (register, login, JWT, refresh, OTP email)
- [x] Task 3 — Client service (profile, vehicles, addresses, mandate)
- [ ] Task 4 — Service catalogue (public `/catalogue/services`, `/catalogue/addons`)
- [ ] Task 5 — Booking core (slots, create/list/cancel/complete, Redis locking)
- [x] Task 6 — Membership module (plans, subscriptions, credits, auto-renewal)
- [ ] Task 7 — Admin endpoints
- [ ] Task 8 — Kafka notifications (email + in-app)
- [ ] Task 9 — Security hardening + integration tests
