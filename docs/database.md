# Database

PostgreSQL 16. Schema managed by Flyway — migrations live in `backend/src/main/resources/db/migration/`.

---

## ER Diagram

```mermaid
erDiagram
    users {
        bigserial id PK
        varchar email
        varchar password_hash
        varchar role
        varchar first_name
        varchar last_name
        varchar phone
        varchar preferred_city
        int search_radius_km
        varchar parts_quality_preference
        timestamp created_at
        timestamp updated_at
    }

    cars {
        bigserial id PK
        bigint owner_id FK
        varchar plate_number
        varchar make
        varchar model
        int year
        varchar engine
        timestamp created_at
    }

    repair_shops {
        bigserial id PK
        bigint owner_id FK
        varchar name
        varchar address
        varchar city
        varchar county
        varchar phone
        varchar email
        varchar website
        varchar google_maps_url
        varchar status
        timestamp created_at
        timestamp updated_at
    }

    shop_service_categories {
        bigserial id PK
        bigint shop_id FK
        varchar category
        decimal price_from
        decimal price_to
        varchar parts_tier
        timestamp last_updated
    }

    job_requests {
        bigserial id PK
        bigint user_id FK
        bigint car_id FK
        text description
        boolean accepts_non_oem
        varchar parts_preference
        varchar search_scope
        varchar city
        varchar county
        text ai_summary
        varchar status
        timestamp offer_deadline
        timestamp created_at
        timestamp updated_at
    }

    shop_offers {
        bigserial id PK
        bigint job_request_id FK
        bigint shop_id FK
        decimal price_estimate
        text breakdown
        varchar parts_tier
        decimal estimated_duration_hours
        timestamp valid_until
        varchar pdf_url
        varchar status
        timestamp created_at
    }

    offer_slots {
        bigserial id PK
        bigint offer_id FK
        timestamp starts_at
        timestamp created_at
    }

    bookings {
        bigserial id PK
        bigint offer_id FK
        bigint job_request_id FK
        bigint user_id FK
        bigint shop_id FK
        timestamp scheduled_at
        boolean confirmed_by_shop
        timestamp shop_confirmed_at
        varchar status
        timestamp created_at
        timestamp updated_at
    }

    reviews {
        bigserial id PK
        bigint booking_id FK
        bigint user_id FK
        bigint shop_id FK
        int rating
        text comment
        timestamp created_at
    }

    user_saved_shops {
        bigserial id PK
        bigint user_id FK
        bigint shop_id FK
        timestamp saved_at
    }

    password_reset_tokens {
        bigserial id PK
        bigint user_id FK
        varchar token
        timestamp expires_at
        boolean used
        timestamp created_at
    }

    guest_estimates {
        bigserial id PK
        varchar plate_number
        text description
        varchar city
        varchar county
        decimal price_from
        decimal price_to
        timestamp created_at
    }

    guest_estimate_results {
        bigserial id PK
        bigint guest_estimate_id FK
        bigint shop_id FK
        decimal price_from
        decimal price_to
    }

    users ||--o{ cars : "owns"
    users ||--o{ repair_shops : "owns"
    users ||--o{ job_requests : "submits"
    users ||--o{ bookings : "has"
    users ||--o{ reviews : "writes"
    users ||--o{ user_saved_shops : "saves"
    users ||--o{ password_reset_tokens : "has"

    repair_shops ||--o{ shop_service_categories : "has"
    repair_shops ||--o{ shop_offers : "submits"
    repair_shops ||--o{ bookings : "receives"
    repair_shops ||--o{ reviews : "receives"
    repair_shops ||--o{ user_saved_shops : "saved by"
    repair_shops ||--o{ guest_estimate_results : "appears in"

    cars ||--o{ job_requests : "described in"

    job_requests ||--o{ shop_offers : "receives"
    job_requests ||--o{ bookings : "leads to"

    shop_offers ||--|| bookings : "accepted as"
    shop_offers ||--o{ offer_slots : "has"

    bookings ||--o| reviews : "reviewed in"

    guest_estimates ||--o{ guest_estimate_results : "produces"
```

---

## Tables

| Table | Description |
|---|---|
| `users` | All user accounts — car owners, shop owners, and admins |
| `cars` | Vehicles registered by car owners |
| `repair_shops` | Shop profiles created when a shop owner registers |
| `shop_service_categories` | Service types and price ranges advertised by a shop |
| `job_requests` | A car owner's repair request, open for offers |
| `shop_offers` | An offer submitted by a shop in response to a job request |
| `offer_slots` | Available appointment time slots attached to an offer |
| `bookings` | Created when a car owner accepts an offer (1-to-1 with offer) |
| `reviews` | Post-service review left by a car owner (1-to-1 with booking) |
| `user_saved_shops` | Bookmarked shops per user |
| `password_reset_tokens` | Single-use tokens for the forgot-password flow |
| `guest_estimates` | Price enquiries submitted without an account |
| `guest_estimate_results` | Per-shop price results for a guest estimate |

---

## Migrations

| Version | Description |
|---|---|
| V1 | Core schema — all main tables |
| V2 | `password_reset_tokens` table |
| V3 | Seed admin user |
| V4 | `user_saved_shops` table + index |
| V5 | User preference columns on `users` |
| V6 | FK indexes for query performance |
| V7 | `offer_slots` table |
| V8 | Status check constraints |
| V9 | Remove hardcoded admin; admin is now seeded at startup via `ADMIN_DEFAULT_PASSWORD` |
