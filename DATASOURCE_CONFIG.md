# INT316D Car Wash — DataSource & Database Configuration

Complete guide to setting up database connection in your IDE and running the application stack.

---

## 🔑 Single Source of Truth — Connection Details

All services use these credentials. Store them in `.env` (local development).

| Parameter | Value | Purpose |
|---|---|---|
| **DBMS** | PostgreSQL 16 | Production-grade relational database |
| **Host** | `localhost` | Connection address |
| **Port** | `5432` | PostgreSQL default port |
| **Database Name** | `int316d_carwash` | Main database (you renamed from aquashine) |
| **Username** | `dev` | DB user account (development) |
| **Password** | `dev` | DB user password (development) |
| **JDBC URL** | `jdbc:postgresql://localhost:5432/int316d_carwash` | Java connection string |
| **Connection Timeout** | 30s | Default timeout for connections |

### Database Schemas (inside the database)

| Schema | Owner | Purpose |
|---|---|---|
| `auth_schema` | auth-service | Users, email OTPs, JWT refresh tokens |
| `client_schema` | client-service | Client profiles, vehicles, addresses, mandates |
| `booking_schema` | booking-service | Bookings, time slots, service selections (Task 5) |
| `membership_schema` | membership-service | Membership plans, subscriptions (Task 6) |

---

## 📋 Option 1: Docker (Recommended — Zero Installation)

### Prerequisites
- Docker & Docker Compose installed (`docker --version`, `docker compose --version`)

### Start the Database

```bash
cd /home/lintshiwe/Documents/Projects1/Car\ Wash\ INT316D/Smart-Car-Wash-System-Backend

# Start only PostgreSQL container
docker compose up -d postgres

# Verify it's running
docker compose ps postgres

# Check health
docker compose logs postgres | grep "accepting connections"
```

### What Happens Automatically
1. **Container Creation**: PostgreSQL 16 Alpine starts as `int316d-postgres`
2. **Data Persistence**: Volume `postgres-data` stores all data
3. **Database Initialization**:
   - Creates database: `int316d_carwash`
   - Creates user: `dev` / password: `dev`
   - Installs extension: `pgcrypto` (for UUID generation)
   - Runs `/init-db/01-init-schemas.sql` (creates all four schemas)

### Stopping / Resetting

```bash
# Stop container (keeps data)
docker compose stop postgres

# Restart container
docker compose start postgres

# Full reset (destroy data volume)
docker compose down -v
docker compose up -d postgres
```

---

## 🔧 Option 2: Local PostgreSQL Installation

Skip this if using Docker (Option 1).

### Linux (Fedora / RHEL)

```bash
# 1. Install PostgreSQL
sudo dnf install -y postgresql-server postgresql-contrib

# 2. Initialize the database cluster
sudo postgresql-setup --initdb

# 3. Start and enable service
sudo systemctl enable --now postgresql

# 4. Create the app user and database
sudo -u postgres psql <<'SQL'
CREATE ROLE dev WITH LOGIN PASSWORD 'dev';
ALTER ROLE dev CREATEDB;
CREATE DATABASE int316d_carwash OWNER dev;
SQL

# 5. Create schemas and extension
psql -h localhost -U dev -d int316d_carwash <<'SQL'
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE SCHEMA IF NOT EXISTS auth_schema       AUTHORIZATION dev;
CREATE SCHEMA IF NOT EXISTS client_schema     AUTHORIZATION dev;
CREATE SCHEMA IF NOT EXISTS booking_schema    AUTHORIZATION dev;
CREATE SCHEMA IF NOT EXISTS membership_schema AUTHORIZATION dev;
SQL
```

### macOS (using Homebrew)

```bash
# 1. Install PostgreSQL
brew install postgresql@16

# 2. Start the service
brew services start postgresql@16

# 3. Create app user and database
createuser -P dev  # enter password: dev
createdb -U dev int316d_carwash

# 4. Create schemas and extension
psql -U dev -d int316d_carwash <<'SQL'
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE SCHEMA IF NOT EXISTS auth_schema       AUTHORIZATION dev;
CREATE SCHEMA IF NOT EXISTS client_schema     AUTHORIZATION dev;
CREATE SCHEMA IF NOT EXISTS booking_schema    AUTHORIZATION dev;
CREATE SCHEMA IF NOT EXISTS membership_schema AUTHORIZATION dev;
SQL
```

---

## 📊 IDE Data Source Setup — IntelliJ IDEA / JetBrains

### Step 1: Open the Database Tool Window
1. In IntelliJ, go to **View** → **Tool Windows** → **Database**
   - Or click the Database icon on the right sidebar
2. Click the **+** icon → **Data Source** → **PostgreSQL**

### Step 2: Fill in Connection Details

| Field | Value |
|---|---|
| **Name** | `INT316D Car Wash (local)` |
| **Host** | `localhost` |
| **Port** | `5432` |
| **User** | `dev` |
| **Password** | `dev` |
| **Database** | `int316d_carwash` |
| **URL** | `jdbc:postgresql://localhost:5432/int316d_carwash` |

### Step 3: Download JDBC Driver
- Click **Test Connection**
- If the PostgreSQL JDBC driver is missing, IntelliJ will prompt you to download it
- Click **"Download Driver"** → **OK**

### Step 4: Configure Visibility
1. Go to the **Schemas** tab
2. **Deselect** "information_schema", "pg_catalog", etc. (keep only):
   - ✅ `auth_schema`
   - ✅ `client_schema`
   - ✅ `booking_schema`
   - ✅ `membership_schema`
   - ✅ `public` (for Flyway schema history)
3. Click **Apply** → **OK**

### Step 5: Verify Connection
In the **Database** tool window, expand your data source. You should see:
```
INT316D Car Wash (local) [PostgreSQL 16]
├── auth_schema
├── client_schema
├── booking_schema
├── membership_schema
└── public
```

**Note**: Tables will appear once services run Flyway migrations (after first boot).

---

## 🖥️ DataSource in Spring Boot Configuration

All microservices automatically connect via `application.yml`:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/int316d_carwash}
    username: ${DB_USER:dev}
    password: ${DB_PASSWORD:dev}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        default_schema: <schema_name>  # each service sets its own schema
```

### How It Works
1. **Environment Variables**: Spring reads `${DB_URL}`, `${DB_USER}`, `${DB_PASSWORD}` from `.env`
2. **Fallback Defaults**: If `.env` is missing, uses the values after the `:`
3. **Schema Isolation**: Each service specifies `default_schema` in its `application.yml`
   - auth-service → `auth_schema`
   - client-service → `client_schema`
   - booking-service → `booking_schema`
   - membership-service → `membership_schema`

---

## ✅ Verification Checklist

### 1. Verify Database Exists
```bash
# Docker users:
docker compose exec postgres psql -U dev -d int316d_carwash -c "\l"

# Local PostgreSQL users:
psql -U dev -d int316d_carwash -c "\l"

# Expected output: int316d_carwash | dev | UTF8 | ...
```

### 2. Verify Schemas Exist
```bash
# Docker:
docker compose exec postgres psql -U dev -d int316d_carwash -c "\dn"

# Local:
psql -U dev -d int316d_carwash -c "\dn"

# Expected output:
# auth_schema       | dev
# booking_schema    | dev
# client_schema     | dev
# membership_schema | dev
# public            | postgres
```

### 3. Verify UUID Extension
```bash
# Docker:
docker compose exec postgres psql -U dev -d int316d_carwash -c "SELECT * FROM pg_extension WHERE extname='pgcrypto';"

# Local:
psql -U dev -d int316d_carwash -c "SELECT * FROM pg_extension WHERE extname='pgcrypto';"

# Expected: pgcrypto extension exists
```

### 4. Connection Ping
```bash
# Docker:
docker compose exec postgres pg_isready -U dev -d int316d_carwash

# Local:
pg_isready -h localhost -U dev -d int316d_carwash

# Expected: accepting connections
```

---

## 🚀 Environment File (`.env`)

### Step 1: Create `.env` from Template
```bash
cd /home/lintshiwe/Documents/Projects1/Car\ Wash\ INT316D/Smart-Car-Wash-System-Backend
touch .env
```

### Step 2: Edit `.env` (Optional — Defaults Work)
```env
# Database (keep these for development)
DB_URL=jdbc:postgresql://localhost:5432/int316d_carwash
DB_USER=dev
DB_PASSWORD=dev

# JWT Secret (CHANGE IN PRODUCTION!)
JWT_SECRET=CHANGE_ME_TO_A_LONG_RANDOM_SECRET_MIN_32_BYTES
JWT_ACCESS_EXPIRY_MS=900000
JWT_REFRESH_EXPIRY_MS=604800000

# Mail (MailHog for local testing)
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_FROM=noreply@int316d.co.za

# Redis (for session caching)
REDIS_HOST=localhost
REDIS_PORT=6379
```

**IMPORTANT**: Never commit `.env` — it's in `.gitignore`.

---

## 🐛 Troubleshooting

| Error | Cause | Solution |
|---|---|---|
| `FATAL: database "int316d_carwash" does not exist` | Docker init script didn't run | `docker compose down -v && docker compose up -d postgres` |
| `password authentication failed for user "dev"` | Wrong `.env` password | Use `dev` for local development |
| `FATAL: Ident authentication failed for user "dev"` | Local PostgreSQL uses `ident/peer` for localhost | Edit `pg_hba.conf` and switch local auth entries (`local all all`, `host all all 127.0.0.1/32`, `host all all ::1/128`) to `scram-sha-256`, then restart PostgreSQL |
| `Connection refused on port 5432` | PostgreSQL not running | Docker: `docker compose start postgres` / Local: `sudo systemctl start postgresql` |
| `Flyway complains about schema "auth_schema" not found` | Schemas weren't created | Re-run init script: see "Verification Checklist" step 2 |
| Can't see tables in IDE | Tables haven't been created yet | Start services first: `mvn -pl auth-service spring-boot:run` |
| Port 5432 already in use | Another PostgreSQL instance running | Change `docker-compose.yml` port to `5433:5432` and update `DB_URL` |

---

## 🚀 Quick Start (Full Stack)

```bash
cd /home/lintshiwe/Documents/Projects1/Car\ Wash\ INT316D/Smart-Car-Wash-System-Backend

# 1. Start all infrastructure
docker compose up -d postgres redis mailhog

# 2. Build backend
./mvnw clean install -DskipTests

# 3. Run each service (in separate terminals)
mvn -pl api-gateway      spring-boot:run
mvn -pl auth-service     spring-boot:run
mvn -pl client-service   spring-boot:run

# 4. Verify in IDE
# - Open Database tool window
# - Expand INT316D Car Wash (local)
# - You should now see tables under each schema
```

### Expected API Endpoints (after all services running)
- API Gateway: `http://localhost:8080/api/v1/...`
- Auth Service: `http://localhost:8081/api/v1/auth`
- Client Service: `http://localhost:8082/api/v1/clients`
- MailHog Web UI: `http://localhost:8025` (test emails)

---

## 📝 Summary of Database Details

**Project Name**: INT316D Car Wash
**Database Name**: `int316d_carwash` (formerly "aquashine")
**DBMS**: PostgreSQL 16
**Default Deployment**: Docker Compose
**User**: `dev` / Password: `dev`
**JDBC URL**: `jdbc:postgresql://localhost:5432/int316d_carwash`
**Schemas**: 4 (auth, client, booking, membership)
**Status**: Ready for development

---

**Last Updated**: 2026-04-22
**Configuration Version**: 1.0
