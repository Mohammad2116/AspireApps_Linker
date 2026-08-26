CREATE TYPE hit_state (
    'LOW', 'NORMAL', 'HIGH', 'VERY_HIGH'
)

CREATE TABLE analysis
(
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    link_id               BIGINT linkId NOT NULL,
    hit_count             BIGINT NOT NULL,
    last_counter_reset_at TIMESTAMP WITH TIME ZONE,
    hit_counter_all_time  BIGINT NOT NULL
)