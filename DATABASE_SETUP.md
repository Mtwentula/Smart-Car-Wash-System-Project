# INT216D Car Wash — Database Setup Guide

Everything you need to create, connect to, and verify the project database.

---

## 1. Connection Details (single source of truth)

| Setting | Value |
|---|---|
| **DBMS** | PostgreSQL 16 |
| **Host** | `localhost` |
| **Port** | `5432` |
| **Database name** | `int216d_carwash` |
| **Username** | `dev` |
| **Password** | `dev` |
| **JDBC URL** | `jdbc:postgresql://localhost:5432/int216d_carwash` |
| **Schemas inside the DB** | `auth_schema`, `client_schema`, `booking_schema`, `membership_schema` |

These match the defaults baked into every service's `application.yml`. Change them in one place (`.env`) and all services pick them up.

---

## 2. Option A — Docker (recommended, zero installation)

The `docker-compose.yml` already describes the full database stack.

```bash
cd Smart-Car-Wash-System-Backend

# Start only PostgreSQL (you can add other services later)
docker compose up -d postgres

# Verify the container is healthy
docker compose ps postgres

# Watch logs if needed
docker compose logs -f postgres
```

The first time the container starts, it automatically:
1. Creates the database `int216d_carwash`.
2. Runs `init-db/01-init-schemas.sql` which:
   - Creates all four schemas (`auth_schema`, `client_schema`, `booking_schema`, `membership_schema`).
   - Installs the `pgcrypto` extension (for `gen_random_uuid()`).
3. Each service's Flyway migration then creates its tables inside its own schema on first boot.

### Stopping / resetting

```bash
# Stop the DB (data is kept)
docker compose stop postgres

# Wipe the database and start fresh
docker compose down -v         # drops the postgres-data volume too
docker compose up -d postgres
```

---

## 3. Option B — Local PostgreSQL install

If you prefer Postgres running on the host:

```bash
# 1. Install (Fedora / RHEL)
sudo dnf install -y postgresql-server postgresql-contrib
sudo postgresql-setup --initdb
sudo systemctl enable --now postgresql

# 2. Create user + database (run as the postgres superuser)
sudo -u postgres psql <<'SQL'
CREATE ROLE dev WITH LOGIN PASSWORD 'dev';
ALTER ROLE dev CREATEDB;
CREATE DATABASE int216d_carwash OWNER dev;
SQL

# 3. Create schemas and extension
psql -h localhost -U dev -d int216d_carwash <<'SQL'
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE SCHEMA IF NOT EXISTS auth_schema       AUTHORIZATION dev;
CREATE SCHEMA IF NOT EXISTS client_schema     AUTHORIZATION dev;
CREATE SCHEMA IF NOT EXISTS booking_schema    AUTHORIZATION dev;
CREATE SCHEMA IF NOT EXISTS membership_schema AUTHORIZATION dev;
SQL
```

---

## 4. JetBrains IDE — Data Source Setup

For viewing data inside IntelliJ / DataGrip while you develop.

1. Open the **Database** tool window (right edge) → `+` → **Data Source** → **PostgreSQL**.
2. Fill in:
   - **Name:** `INT216D Car Wash (local)`
   - **Host:** `localhost`
   - **Port:** `5432`
   - **User:** `dev`
   - **Password:** `dev`
   - **Database:** `int216d_carwash`
   - **URL:** `jdbc:postgresql://localhost:5432/int216d_carwash`
3. Click **Test Connection**. If the JDBC driver is missing, IntelliJ offers to download it — accept.
4. Open **Schemas** tab, tick: `auth_schema`, `client_schema`, `booking_schema`, `membership_schema`, `public`.
5. Apply → OK.

You should now see the four schemas in the tree. Tables appear once the services run Flyway on first boot.

---

## 5. Verifying everything works

```bash
# Quick ping
docker compose exec postgres pg_isready -U dev -d int216d_carwash
# -> accepting connections

# Open a psql session
docker compose exec postgres psql -U dev -d int216d_carwash

# Inside psql:
\dn                     # list schemas — you should see all four
\dt auth_schema.*       # list tables inside auth_schema (empty until auth-service runs)
\q                      # quit
```

After you start `auth-service` and `client-service` for the first time, Flyway will create the tables. You can verify:

```sql
-- In psql, after services have started once:
\dt auth_schema.*
-- users, email_otps, refresh_tokens, flyway_schema_history

\dt client_schema.*
-- clients, vehicles, addresses, mandates, flyway_schema_history
```

---

## 6. Environment overrides (no secrets in git)

Create `.env` and change values there. The services read these variables at boot:

```bash
touch .env
```

```env
DB_URL=jdbc:postgresql://localhost:5432/int216d_carwash
DB_USER=dev
DB_PASSWORD=dev
```

**Never commit `.env`** — it's already in `.gitignore`.

---

## 7. Troubleshooting cheat-sheet

| Symptom | Fix |
|---|---|
| `FATAL: database "int216d_carwash" does not exist` | Run `docker compose down -v && docker compose up -d postgres` to re-run init scripts. |
| `password authentication failed for user "dev"` | Wrong password in `.env`. Default is `dev`. |
| `Connection refused` | Postgres isn't running. Check `docker compose ps postgres`. |
| Flyway complains about missing schema | Confirm `init-db/01-init-schemas.sql` ran — `SELECT schema_name FROM information_schema.schemata;` |
| Need to reset everything | `docker compose down -v` (destroys the volume, then re-creates). |
| Port 5432 already in use | Another Postgres is running locally. Either stop it or change the host port in `docker-compose.yml` (e.g. `5433:5432`) and update `DB_URL`. |
