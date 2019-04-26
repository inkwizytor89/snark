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
  cp          integer   not null,

  lm   integer,
  cm   integer,
  kr   integer,
  ow   integer,
  pan   integer,
  bom   integer,
  ni   integer,
  gs   integer,
  mt   integer,
  dt   integer,
  kol   integer,
  rec   integer,
  son   integer,
  sat   integer,

  wr   integer,
  ldl   integer,
  cdl   integer,
  dg   integer,
  dj   integer,
  wp   integer,
  mpo   integer,
  dpo   integer,
  pr   integer,
  mr   integer
);
