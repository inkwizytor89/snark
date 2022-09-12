create table collections(
    id bigserial not null primary key,
    source_id bigint not null references colonies,
    type text,
    start timestamp default null,
  updated    timestamp default now()
);
