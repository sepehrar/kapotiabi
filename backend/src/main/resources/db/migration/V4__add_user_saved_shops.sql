CREATE TABLE user_saved_shops (
    id       BIGSERIAL PRIMARY KEY,
    user_id  BIGINT NOT NULL REFERENCES users(id),
    shop_id  BIGINT NOT NULL REFERENCES repair_shops(id),
    saved_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, shop_id)
);
