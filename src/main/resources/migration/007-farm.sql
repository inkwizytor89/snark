create table farm_waves(
    id bigserial not null primary key,
    universe_id bigint not null references universes,
    start timestamp not null,
    spy_requests_id bigint,
    war_requests_id bigint
);
