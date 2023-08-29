create table cache_entries (
    id bigserial not null primary key,
    created timestamp not null default now(),
    updated timestamp not null default now(),
    key text not null,
    value text 
);
