create table galaxy
(
  id         bigserial not null
    constraint exploration_pkey
    primary key,
  univers_id bigint
    constraint exploration_universes_id_fk
    references universes
    constraint fksyp2s0s9nocynm6vw3nqui54a
    references universes,
  galaxy     integer,
  system     integer,
  updated    timestamp default now()
);

create unique index exploration_id_uindex
  on galaxy (id);