create table errors(
    id bigserial not null primary key,
    message text,
    stack_trace text,
    created timestamp not null,
    action text,
    value text,
    page text,
  updated    timestamp default now()
);
