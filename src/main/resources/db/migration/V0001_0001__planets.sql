--create table planets
--(
--  id          bigserial   not null
--    constraint planets_pkey
--    primary key,
--  universe_id bigint      not null
--    constraint fkcj4uu0m13tql66hftb6eq7ypg
--    references universes
--    constraint targets_universe_id_fkey
--    references universes,
--  galaxy      integer     not null,
--  system      integer     not null,
--  position    integer     not null,
--  metal       integer,
--  crystal     integer,
--  deuterium   integer,
--  power       integer,
--
--  lm   integer,
--  cm   integer,
--  kr   integer,
--  ow   integer,
--  pan   integer,
--  bom   integer,
--  ni   integer,
--  gs   integer,
--  mt   integer,
--  dt   integer,
--  kol   integer,
--  rec   integer,
--  son   integer,
--  sat   integer,
--
--  wr   integer,
--  ldl   integer,
--  cdl   integer,
--  dg   integer,
--  dj   integer,
--  wp   integer,
--  mpo   integer,
--  dpo   integer,
--  pr   integer,
--  mr   integer
--);

create table colonies
(
  id          bigserial   not null
    constraint colonies_pkey
    primary key,
  universe_id bigint      not null
    constraint fkcj4uu0m13tql66hftb6eq7ypg
    references universes
    constraint targets_universe_id_fkey
    references universes,
  galaxy      integer     not null,
  system      integer     not null,
  position    integer     not null,
  metal       integer,
  crystal     integer,
  deuterium   integer,
  power       integer,

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
  mr   integer,


  cp          integer   not null,
  collecting_order    integer
);

create table targets
(
  id          bigserial   not null
    constraint targets_pkey
    primary key,
  universe_id bigint      not null
    constraint fkcj4uu0m13tql66hftb6eq7ypg
    references universes
    constraint targets_universe_id_fkey
    references universes,
  galaxy      integer     not null,
  system      integer     not null,
  position    integer     not null,
  metal       integer,
  crystal     integer,
  deuterium   integer,
  power       integer,

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
  mr   integer,


  resources   integer,
  type        varchar(15) not null,
  fleet_sum   bigint default 0,
  defense_sum bigint default 0,
  spy_level integer default 4,
  updated     timestamp
);
