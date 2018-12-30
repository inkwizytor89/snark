create table planets
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
  power       integer,
  type        varchar(15) not null,
  fleet_sum   bigint default 0,
  defense_sum bigint default 0,
  updated     timestamp
);