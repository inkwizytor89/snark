create table sources
(
  id          bigserial not null
    constraint sources_pkey
    primary key,
  universe_id bigint    not null
    constraint fkkax8vhf2ae5b28h7itb4kn7wc
    references universes
    constraint sources_universe_id_fkey
    references universes,
  galaxy      integer   not null,
  system      integer   not null,
  position    integer   not null,
  metal       integer,
  crystal     integer,
  deuterium   integer,
  resources   integer,
  power       integer,
  cp          integer   not null
);
