create table errors(
    id bigserial not null primary key,
    universe_id bigint not null references universes,
    message text,
    stack_trace text,
    created timestamp not null,
    action text,
    value text,
    page text
);
