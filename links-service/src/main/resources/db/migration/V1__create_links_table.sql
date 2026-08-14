CREATE SEQUENCE IF NOT EXISTS links_seq START WITH 1920373 INCREMENT 1;

CREATE TYPE link_status AS ENUM (
    'ACTIVE', 'DISABLED', 'EXPIRED'
    );

CREATE TABLE IF NOT EXISTS links
(
    id           BIGINT PRIMARY KEY,
    title        VARCHAR(254)  NOT NULL,
    original_url VARCHAR(1024) NOT NULL,
    short_url    VARCHAR(10)   NOT NULL,
    user_id      UUID          NOT NULL,
    status       link_status   NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ   NULL,
    expires_at   TIMESTAMPTZ   NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_links_user
    ON links (user_id);