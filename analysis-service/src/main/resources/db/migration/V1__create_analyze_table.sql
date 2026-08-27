CREATE TYPE hit_state_type AS ENUM (
    'LOW', 'NORMAL', 'HIGH', 'VERY_HIGH'
);

CREATE TABLE IF NOT EXISTS analysis
(
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    shorted_url
    VARCHAR
(
    12
) NOT NULL,
    hit_count             BIGINT NOT NULL,
    all_time_hit_count BIGINT NOT NULL,
    counter_reset_at   TIMESTAMP WITH TIME ZONE,
    hit_state hit_state_type default 'NORMAL'
                                     );