CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    token_hash  VARCHAR(512) NOT NULL UNIQUE,
    device_ip   INET         NOT NULL,
    device_name VARCHAR(254) NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    revoked     BOOLEAN DEFAULT FALSE,
    revoked_at  TIMESTAMPTZ  NULL,
    user_id     UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE
)