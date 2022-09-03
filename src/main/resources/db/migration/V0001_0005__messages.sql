create table messages(
    id bigserial not null primary key,
    created timestamp not null,
    message_id bigint,
    type text not null,
    content text
);
