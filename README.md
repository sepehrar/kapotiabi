# Kapotiabi

Car repair marketplace for Estonia. Car owners describe their problem, receive price estimates from repair shops, and book appointments — all in one place.

---

## Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 4.0.6, Java 21, Spring Security, JPA/Hibernate |
| Frontend | Next.js 15, TypeScript, Tailwind CSS, next-intl |
| Database | PostgreSQL 17, Flyway migrations |
| Auth | JWT (stateless, HMAC/shared secret, HS256-style) |
| AI search | Groq API (server-side, Next.js API route) |
| Infrastructure | Docker, Docker Compose |

---

## Quick Start

### Docker (recommended)

```bash
cp .env.example .env
# Fill in JWT_SECRET and ADMIN_DEFAULT_PASSWORD in .env

docker compose up --build
```

| Service | URL |
|---|---|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |

### Local development

**Backend:**
```bash
# Requires a running PostgreSQL instance
cd backend
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

The `dev` profile supplies the JWT secret automatically. See [docs/auth.md](docs/auth.md) for all environment variables.

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

---

## Environment Variables

| Variable | Required | Description |
|---|---|---|
| `JWT_SECRET` | Yes | JWT signing key — no default, app fails without it |
| `ADMIN_DEFAULT_PASSWORD` | Yes | Initial admin password set at startup |
| `ADMIN_EMAIL` | No | Admin email (default: `admin@kapotiabi.ee`) |
| `APP_CORS_ALLOWED_ORIGINS` | No | Allowed CORS origins (default: `http://localhost:3000`) |
| `GROQ_API_KEY` | No | Enables AI-powered repair search in the frontend |

---

## Documentation

| Document | Description |
|---|---|
| [Architecture](docs/architecture.md) | System components, request flow, design decisions |
| [Database diagram](docs/database.md) | ER diagram and table descriptions |
| [Application flow](docs/flow.md) | End-to-end user journeys and booking lifecycle |
| [Authentication](docs/auth.md) | Auth endpoints, JWT format, running locally |
| [Authorization](docs/authorization.md) | Role permissions, ownership rules, booking state machine |
| [Saved shops](docs/saved-shops.md) | Bookmarks feature |
| [User preferences](docs/user-preferences.md) | Search preferences |
| [Offer slots](docs/offer-slots.md) | Availability slots on offers |

---

## Testing

```bash
cd backend

# Run all tests
./mvnw test

# Check code formatting
./mvnw spotless:check

# Fix formatting
./mvnw spotless:apply
```

Integration tests use Testcontainers (PostgreSQL). Unit tests use H2 in-memory.

---

## Team

| Name | Role |
|---|---|
| Ingvar Leerimaa | Backend |
| Mahdiyeh Sepehrar | Backend |
| Andrii Lytvyn | Backend |
| Bohdan Podziubanchuk | Full-stack |
| Olga Tenison | Frontend |
