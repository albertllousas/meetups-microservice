CREATE TABLE groups (
    id      UUID        NOT NULL,
    title   TEXT        NOT NULL,
    members UUID[]      ,
    meetups UUID[]      ,
    created TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT pk_group PRIMARY KEY (id)
);

CREATE TABLE meetups (
   id           UUID                NOT NULL,
   topic        TEXT                NOT NULL,
   details      TEXT                NOT NULL,
   hosted_by    UUID                NOT NULL,
   on_date      TIMESTAMP WITH TIME ZONE NOT NULL,
   group_id     UUID                ,   -- No FK on purpose, integrity will be handled at app level with DDD patterns
   attendees    UUID[]              ,
   meetup_type  TEXT                NOT NULL,
   link_name    TEXT                ,
   link_url     TEXT                ,
   address      TEXT                ,
   status       TEXT                NOT NULL,
   cancel_reason TEXT               ,
   rating_stars DECIMAL             ,
   rating_votes INT                 ,
   created      TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
   CONSTRAINT pk_meetup PRIMARY KEY (id)
);

CREATE TABLE outbox
(
    id            UUID PRIMARY KEY,
    aggregate_id  UUID                     NOT NULL,
    event_payload BYTEA                    NOT NULL,
    stream        TEXT                     NOT NULL,
    created       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);


-- CREATE TABLE public.people_replication
-- (
--     id         UUID        NOT NULL,
--     status     TEXT        NOT NULL,
--     first_name TEXT        NOT NULL,
--     last_name  TEXT        NOT NULL,
--     joined_at  TIMESTAMPTZ NOT NULL,
--     CONSTRAINT pk_people_replication PRIMARY KEY (id)
-- );
