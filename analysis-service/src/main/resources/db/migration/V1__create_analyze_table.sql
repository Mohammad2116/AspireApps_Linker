CREATE TYPE hit_state (
    'LOW', 'NORMAL', 'HIGH', 'VERY_HIGH'
)

CREATE TABLE analysis
(
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    link_id               BIGINT linkId NOT NULL,
    hit_count             BIGINT NOT NULL,
    all_time_hit_count BIGINT NOT NULL,
    counter_reset_at   TIMESTAMP WITH TIME ZONE,
)