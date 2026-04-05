-- ───────────────────────────────────────────
-- TalTech Car Repair Marketplace — DB Schema
-- ───────────────────────────────────────────

CREATE TABLE users (
    id            SERIAL PRIMARY KEY,
    email         VARCHAR NOT NULL UNIQUE,
    password_hash VARCHAR NOT NULL,
    role          VARCHAR NOT NULL,  -- car_owner | shop_owner | admin
    first_name    VARCHAR,
    last_name     VARCHAR,
    phone         VARCHAR,
    created_at    TIMESTAMP DEFAULT NOW(),
    updated_at    TIMESTAMP DEFAULT NOW()
);

CREATE TABLE cars (
    id           SERIAL PRIMARY KEY,
    owner_id     INTEGER NOT NULL REFERENCES users(id),
    plate_number VARCHAR NOT NULL,  -- Used to fetch data from ARK. Handle per GDPR.
    make         VARCHAR,
    model        VARCHAR,
    year         INTEGER,
    engine       VARCHAR,
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE TABLE repair_shops (
    id              SERIAL PRIMARY KEY,
    owner_id        INTEGER NOT NULL REFERENCES users(id),
    name            VARCHAR NOT NULL,
    address         VARCHAR,
    city            VARCHAR,
    county          VARCHAR,
    phone           VARCHAR,
    email           VARCHAR,
    website         VARCHAR,
    google_maps_url VARCHAR,
    status          VARCHAR,  -- active | draft | needs_update
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE shop_service_categories (
    id           SERIAL PRIMARY KEY,
    shop_id      INTEGER NOT NULL REFERENCES repair_shops(id),
    category     VARCHAR NOT NULL,  -- oil_change | tire_change | diagnostics | bodywork | etc
    price_from   DECIMAL,
    price_to     DECIMAL,
    parts_tier   VARCHAR,  -- OEM | equivalent | budget
    last_updated TIMESTAMP DEFAULT NOW()
);

CREATE TABLE job_requests (
    id               SERIAL PRIMARY KEY,
    user_id          INTEGER NOT NULL REFERENCES users(id),
    car_id           INTEGER NOT NULL REFERENCES cars(id),
    description      TEXT NOT NULL,
    accepts_non_oem  BOOLEAN DEFAULT FALSE,
    parts_preference VARCHAR,  -- OEM | equivalent | budget
    search_scope     VARCHAR,  -- city | county | country
    city             VARCHAR,
    county           VARCHAR,
    ai_summary       TEXT,     -- AI-generated synopsis sent to shops
    status           VARCHAR,  -- pending | offers_received | booked | completed | cancelled
    offer_deadline   TIMESTAMP,
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW()
);

CREATE TABLE shop_offers (
    id                       SERIAL PRIMARY KEY,
    job_request_id           INTEGER NOT NULL REFERENCES job_requests(id),
    shop_id                  INTEGER NOT NULL REFERENCES repair_shops(id),
    price_estimate           DECIMAL NOT NULL,
    breakdown                TEXT,     -- Detailed cost breakdown
    parts_tier               VARCHAR,  -- OEM | equivalent | budget
    estimated_duration_hours DECIMAL,
    valid_until              TIMESTAMP,
    pdf_url                  VARCHAR,  -- Link to downloadable PDF offer
    status                   VARCHAR,  -- pending | accepted | rejected | expired
    created_at               TIMESTAMP DEFAULT NOW()
);

CREATE TABLE bookings (
    id                SERIAL PRIMARY KEY,
    offer_id          INTEGER NOT NULL REFERENCES shop_offers(id),
    job_request_id    INTEGER NOT NULL REFERENCES job_requests(id),
    user_id           INTEGER NOT NULL REFERENCES users(id),
    shop_id           INTEGER NOT NULL REFERENCES repair_shops(id),
    scheduled_at      TIMESTAMP,
    confirmed_by_shop BOOLEAN DEFAULT FALSE,
    shop_confirmed_at TIMESTAMP,
    status            VARCHAR,  -- awaiting_shop | confirmed | in_progress | completed | cancelled
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW()
);

CREATE TABLE reviews (
    id         SERIAL PRIMARY KEY,
    booking_id INTEGER NOT NULL REFERENCES bookings(id),
    user_id    INTEGER NOT NULL REFERENCES users(id),
    shop_id    INTEGER NOT NULL REFERENCES repair_shops(id),
    rating     INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE guest_estimates (
    id           SERIAL PRIMARY KEY,
    plate_number VARCHAR,  -- Not stored long-term — GDPR
    description  TEXT,
    city         VARCHAR,
    county       VARCHAR,
    price_from   DECIMAL,
    price_to     DECIMAL,
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE TABLE guest_estimate_results (
    id                SERIAL PRIMARY KEY,
    guest_estimate_id INTEGER NOT NULL REFERENCES guest_estimates(id),
    shop_id           INTEGER NOT NULL REFERENCES repair_shops(id),
    price_from        DECIMAL,
    price_to          DECIMAL
);
