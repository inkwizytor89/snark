create table galaxy
(
  id         bigserial not null
    constraint exploration_pkey
    primary key,
  universe_id bigint,
  galaxy     integer,
  system     integer,
  updated    timestamp default now()
);

create unique index exploration_id_uindex
  on galaxy (id);
