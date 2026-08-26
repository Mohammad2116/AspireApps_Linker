CREATE TYPE hit_state AS ENUM (
    'LOW', 'NORMAL', 'HIGH', 'VERY_HIGH'
)

CREATE TABLE analysis
(
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    shorted_url    VARCHAR(12) NOT NULL,
    hit_count             BIGINT NOT NULL,
    all_time_hit_count BIGINT NOT NULL,
    counter_reset_at   TIMESTAMP WITH TIME ZONE,
    link_hit_state hit_state default 'NORMAL'
)