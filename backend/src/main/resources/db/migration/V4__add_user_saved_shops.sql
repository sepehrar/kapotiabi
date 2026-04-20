CREATE TABLE user_saved_shops (
    id       BIGSERIAL PRIMARY KEY,
    user_id  BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    shop_id  BIGINT NOT NULL REFERENCES repair_shops(id) ON DELETE CASCADE,
    saved_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, shop_id)
);
CREATE INDEX idx_user_saved_shops_user_saved_at ON user_saved_shops (user_id, saved_at DESC);
