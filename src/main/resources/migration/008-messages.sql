create table messages(
    id bigserial not null primary key,
    universe_id bigint not null references universes,
    created timestamp not null,
    message_id bigint,
    type text not null,
    content text
);
