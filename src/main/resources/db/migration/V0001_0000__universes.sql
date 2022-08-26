create table universes
(
  id               bigserial           not null
    constraint universes_pkey
    primary key,
  name             text,
  url              text,
  login            text,
  pass             text,
  mode             text,
  config           text,
  galaxy_max       integer default 6   not null,
  exploration_area integer default 9   not null,
  system_max       integer default 499 not null
);