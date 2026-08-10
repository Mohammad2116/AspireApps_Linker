CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TYPE user_role AS ENUM (
    'ADMIN', 'USER'
    );

CREATE TYPE subscription_status AS ENUM (
    'FREE', 'PREMIUM'
    );

CREATE TABLE IF NOT EXISTS users
(
    id                    UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    username              VARCHAR(100) UNIQUE NOT NULL CHECK (
        length(username) >= 3
        ),
    email                 VARCHAR(254) UNIQUE NOT NULL,
    password              VARCHAR(512)        NOT NULL,
    role                  user_role           NOT NULL DEFAULT 'USER',
    status                subscription_status NOT NULL DEFAULT 'FREE',
    created_at            TIMESTAMPTZ         NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ         NULL,
    last_login_at         TIMESTAMPTZ         NULL,
    enabled               BOOLEAN             NOT NULL DEFAULT FALSE,
    email_verified        BOOLEAN             NOT NULL DEFAULT FALSE,
    failed_login_attempts INTEGER             NOT NULL DEFAULT 0,
    locked_until          TIMESTAMPTZ         NULL
)

