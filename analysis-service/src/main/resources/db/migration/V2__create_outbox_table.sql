CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE event_state AS ENUM (
    'PENDING', 'PROCEED'
    );

CREATE TABLE IF NOT EXISTS outbox_messages
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id BIGINT      NOT NULL,
    topic        VARCHAR(50) NOT NULL,
    status       event_state      default 'PENDING',
    payload      TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NULL
);

CREATE INDEX IF NOT EXISTS idx_outbox_pending
    ON outbox_messages (created_at)
    WHERE status = 'PENDING';