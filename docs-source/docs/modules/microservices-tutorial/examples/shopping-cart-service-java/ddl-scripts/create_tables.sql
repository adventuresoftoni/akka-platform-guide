DROP TABLE IF EXISTS public.journal;

CREATE TABLE IF NOT EXISTS public.journal (
    ordering BIGSERIAL,
    persistence_id VARCHAR(255) NOT NULL,
    sequence_number BIGINT NOT NULL,
    deleted BOOLEAN DEFAULT FALSE NOT NULL,
    tags VARCHAR(255) DEFAULT NULL,
    message BYTEA NOT NULL,
    PRIMARY KEY(persistence_id, sequence_number));

CREATE UNIQUE INDEX journal_ordering_idx ON public.journal(ordering);

DROP TABLE IF EXISTS public.snapshot;

CREATE TABLE IF NOT EXISTS public.snapshot (
    persistence_id VARCHAR(255) NOT NULL,
    sequence_number BIGINT NOT NULL,
    created BIGINT NOT NULL,
    snapshot BYTEA NOT NULL,
    PRIMARY KEY(persistence_id, sequence_number));

create table if not exists public."AKKA_PROJECTION_OFFSET_STORE" (
    "PROJECTION_NAME" VARCHAR(255) NOT NULL,
    "PROJECTION_KEY" VARCHAR(255) NOT NULL,
    "CURRENT_OFFSET" VARCHAR(255) NOT NULL,
    "MANIFEST" VARCHAR(4) NOT NULL,
    "MERGEABLE" BOOLEAN NOT NULL,
    "LAST_UPDATED" BIGINT NOT NULL
    );

create index "PROJECTION_NAME_INDEX" on public."AKKA_PROJECTION_OFFSET_STORE" ("PROJECTION_NAME");

alter table public."AKKA_PROJECTION_OFFSET_STORE"
    add constraint "PK_PROJECTION_ID" primary key("PROJECTION_NAME","PROJECTION_KEY");