CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE event_state AS ENUM (
    'PENDING', 'PROCEED'
    );

CREATE INDEX IF NOT EXISTS idx_outbox_pending
    ON outbox_messages (create_at)
    WHERE status = 'PENDING';

CREATE TABLE IF NOT EXISTS outbox_messages
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id BIGINT      NOT NULL,
    topic        VARCHAR(50) NOT NULL,
    status       event_state      default 'PENDING',
    payload      TEXT        NOT NULL,
    create_at    TIMESTAMPTZ NOT NULL,
    update_at    TIMESTAMPTZ NULL
);