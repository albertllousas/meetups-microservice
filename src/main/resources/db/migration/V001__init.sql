CREATE TABLE public.groups (
    id      UUID        NOT NULL,
    title   TEXT        NOT NULL,
    members UUID[]      ,
    meetups UUID[]      ,
    created TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT pk_group PRIMARY KEY (id)
);

-- CREATE TABLE public.outbox
-- (
--     id            UUID PRIMARY KEY,
--     aggregate_id  UUID                     NOT NULL,
--     event_payload BYTEA                    NOT NULL,
--     stream        TEXT                     NOT NULL,
--     created       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
-- );
--
--
-- CREATE TABLE public.people_replication
-- (
--     id         UUID        NOT NULL,
--     status     TEXT        NOT NULL,
--     first_name TEXT        NOT NULL,
--     last_name  TEXT        NOT NULL,
--     joined_at  TIMESTAMPTZ NOT NULL,
--     CONSTRAINT pk_people_replication PRIMARY KEY (id)
-- );
