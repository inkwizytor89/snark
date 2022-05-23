create table collections(
    id bigserial not null primary key,
    universe_id bigint not null references universes,
    source_id bigint not null references sources,
    type text,
    start timestamp default null
);
